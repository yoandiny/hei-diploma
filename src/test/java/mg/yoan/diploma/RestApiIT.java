package mg.yoan.diploma;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import mg.yoan.diploma.domain.Course;
import mg.yoan.diploma.domain.Group;
import mg.yoan.diploma.domain.Promotion;
import mg.yoan.diploma.domain.Student;
import mg.yoan.diploma.domain.Teacher;
import mg.yoan.diploma.endpoint.rest.controller.admin.StudentSummaryResponse;
import mg.yoan.diploma.endpoint.rest.controller.auth.LoginRequest;
import mg.yoan.diploma.endpoint.rest.controller.auth.LoginResponse;
import mg.yoan.diploma.endpoint.rest.controller.student.TranscriptResponse;
import mg.yoan.diploma.repository.model.JUser;
import mg.yoan.diploma.service.TeacherGradeService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

class RestApiIT extends DiplomaIT {

  @Autowired private TestRestTemplate restTemplate;
  @Autowired private TeacherGradeService teacherGradeService;

  @Test
  void login_succeeds_with_correct_credentials() {
    Teacher teacher = newTeacher();

    ResponseEntity<LoginResponse> response =
        restTemplate.postForEntity(
            "/auth/login",
            new LoginRequest(teacher.getUser().getEmail(), PASSWORD),
            LoginResponse.class);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    var body = response.getBody();
    assertTrue(body != null && body.accessToken() != null && !body.accessToken().isBlank());
    assertEquals(mg.yoan.diploma.domain.Role.TEACHER, body.role());
  }

  @Test
  void login_rejects_wrong_password() {
    Teacher teacher = newTeacher();

    ResponseEntity<LoginResponse> response =
        restTemplate.postForEntity(
            "/auth/login",
            new LoginRequest(teacher.getUser().getEmail(), "wrong-password"),
            LoginResponse.class);

    assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
  }

  @Test
  void login_rejects_unknown_email() {
    ResponseEntity<LoginResponse> response =
        restTemplate.postForEntity(
            "/auth/login",
            new LoginRequest("nobody@mail.hei.school", PASSWORD),
            LoginResponse.class);

    assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
  }

  @Test
  void student_reads_their_own_transcript_over_http() {
    Promotion promotion = newPromotion();
    Group group = newGroup(promotion);
    Course course = newCourse();
    Teacher teacher = newTeacher();
    Student student = newStudent(promotion, group);
    assign(course, teacher, group);
    var exam =
        teacherGradeService.createExam(
            teacher.getId().toString(),
            course.getId().toString(),
            Instant.parse("2026-06-01T08:00:00Z"),
            BigDecimal.ONE,
            List.of(group.getId().toString()));
    teacherGradeService.saveGrade(
        teacher.getId().toString(),
        exam.examId(),
        student.getId().toString(),
        new BigDecimal("14.00"));
    teacherGradeService.submitExam(teacher.getId().toString(), exam.examId());

    String token = login(student.getUser().getEmail());

    ResponseEntity<TranscriptResponse> response =
        restTemplate.exchange(
            "/students/me/transcript",
            org.springframework.http.HttpMethod.GET,
            authenticated(token),
            TranscriptResponse.class);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    var body = response.getBody();
    assertTrue(body != null);
    assertEquals(student.getStudentNumber(), body.studentNumber());
    assertEquals(1, body.grades().size());
    assertEquals(new BigDecimal("14.00"), body.weightedAverage());
  }

  @Test
  void teacher_cannot_read_a_student_transcript_endpoint() {
    Teacher teacher = newTeacher();
    String token = login(teacher.getUser().getEmail());

    ResponseEntity<String> response =
        restTemplate.exchange(
            "/students/me/transcript",
            org.springframework.http.HttpMethod.GET,
            authenticated(token),
            String.class);

    assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
  }

  @Test
  void anonymous_request_is_rejected() {
    ResponseEntity<String> response =
        restTemplate.exchange(
            "/students/me/transcript",
            org.springframework.http.HttpMethod.GET,
            HttpEntity.EMPTY,
            String.class);

    assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
  }

  @Test
  void admin_lists_students_over_http() {
    Promotion promotion = newPromotion();
    Group group = newGroup(promotion);
    Student student = newStudent(promotion, group);
    JUser admin = newAdmin();
    String token = login(admin.getEmail());

    ResponseEntity<StudentSummaryResponse[]> response =
        restTemplate.exchange(
            "/admin/students",
            org.springframework.http.HttpMethod.GET,
            authenticated(token),
            StudentSummaryResponse[].class);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    var body = response.getBody();
    assertTrue(body != null);
    assertTrue(
        java.util.Arrays.stream(body)
            .anyMatch(s -> s.studentNumber().equals(student.getStudentNumber())));
  }

  @Test
  void student_cannot_list_admin_students_endpoint() {
    Promotion promotion = newPromotion();
    Student student = newStudent(promotion, newGroup(promotion));
    String token = login(student.getUser().getEmail());

    ResponseEntity<String> response =
        restTemplate.exchange(
            "/admin/students",
            org.springframework.http.HttpMethod.GET,
            authenticated(token),
            String.class);

    assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
  }

  private String login(String email) {
    ResponseEntity<LoginResponse> response =
        restTemplate.postForEntity(
            "/auth/login", new LoginRequest(email, PASSWORD), LoginResponse.class);
    var body = response.getBody();
    assertTrue(body != null);
    return body.accessToken();
  }

  private HttpEntity<Void> authenticated(String token) {
    HttpHeaders headers = new HttpHeaders();
    headers.setBearerAuth(token);
    return new HttpEntity<>(headers);
  }
}
