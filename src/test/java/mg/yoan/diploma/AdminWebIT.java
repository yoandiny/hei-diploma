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
import mg.yoan.diploma.service.AdminGradeService;
import mg.yoan.diploma.service.CourseAssignmentService;
import mg.yoan.diploma.service.TeacherGradeService;
import mg.yoan.diploma.service.TeacherGradeService.ExamOption;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

class AdminWebIT extends WebDiplomaIT {

  @Autowired private TeacherGradeService teacherGradeService;
  @Autowired private CourseAssignmentService assignmentService;
  @Autowired private AdminGradeService adminGradeService;

  @Test
  void admin_can_open_main_pages() {
    String token = adminJwt();

    assertContains(htmlGet("/admin/dashboard.html", token).getBody(), "Dashboard admin");
    assertContains(htmlGet("/admin/students/list.html", token).getBody(), "Étudiants");
    assertContains(htmlGet("/admin/teachers/list.html", token).getBody(), "Enseignants");
    assertContains(htmlGet("/admin/courses/list.html", token).getBody(), "Gestion des cours");
    assertContains(htmlGet("/admin/groups/list.html", token).getBody(), "Groupes");
    assertContains(htmlGet("/admin/grades/home.html", token).getBody(), "Gestion des notes");
    assertContains(htmlGet("/admin/grades/history.html", token).getBody(), "Historique");
    assertContains(htmlGet("/admin/promotion/results.html", token).getBody(), "Diplômés");
    assertContains(htmlGet("/admin/students/form.html", token).getBody(), "Nouvel étudiant");
    assertContains(htmlGet("/admin/teachers/form.html", token).getBody(), "Nouvel enseignant");
    assertContains(htmlGet("/admin/courses/form.html", token).getBody(), "Cours");
    assertContains(htmlGet("/admin/groups/form.html", token).getBody(), "Nouveau groupe");
    assertContains(htmlGet("/admin/promotions/form.html", token).getBody(), "Nouvelle promotion");
  }

  @Test
  void admin_can_manage_courses_groups_and_promotions() {
    String token = adminJwt();
    Promotion promotion = newPromotion();
    String groupRef = "GW" + uid();

    htmlPost(
        "/admin/groups/save",
        token,
        form("ref", groupRef, "promotionId", promotion.getId().toString()));
    Group group =
        groupService.listAll().stream()
            .filter(item -> groupRef.equals(item.getRef()))
            .findFirst()
            .orElseThrow();
    assertContains(
        htmlGet("/admin/groups/form.html?id=" + group.getId(), token).getBody(), groupRef);

    String promoLabel = "PL" + uid();
    htmlPost(
        "/admin/promotions/save",
        token,
        form("label", promoLabel, "startYear", "2024", "endYear", "2027"));
    Promotion createdPromotion =
        promotionService.listAll().stream()
            .filter(item -> promoLabel.equals(item.getLabel()))
            .findFirst()
            .orElseThrow();
    assertContains(
        htmlGet("/admin/promotions/form.html?id=" + createdPromotion.getId(), token).getBody(),
        promoLabel);

    String courseRef = "CW" + uid();
    htmlPost(
        "/admin/courses/save", token, form("ref", courseRef, "title", "Cours web", "credits", "6"));
    Course course =
        courseService.listAll().stream()
            .filter(item -> courseRef.equals(item.getRef()))
            .findFirst()
            .orElseThrow();
    assertContains(
        htmlGet("/admin/courses/form.html?id=" + course.getId(), token).getBody(), courseRef);

    htmlPost(
        "/admin/courses/save",
        token,
        form(
            "id",
            course.getId().toString(),
            "ref",
            courseRef,
            "title",
            "Cours web modifié",
            "credits",
            "8"));
    assertEquals("Cours web modifié", courseService.getById(course.getId().toString()).getTitle());

    Teacher teacher = newTeacher();
    MultiValueMap<String, String> assignmentForm = new LinkedMultiValueMap<>();
    assignmentForm.add("teacherId", teacher.getId().toString());
    assignmentForm.add("groupIds", group.getId().toString());
    htmlPost("/admin/courses/" + course.getId() + "/assignments", token, assignmentForm);
    assertContains(
        htmlGet("/admin/courses/assign-groups.html?courseId=" + course.getId(), token).getBody(),
        teacher.getUser().getFullName());
    assertContains(
        htmlGet("/admin/courses/" + course.getId() + "/assignments", token).getBody(),
        teacher.getUser().getFullName());

    var assignment =
        assignmentService.listByCourse(course.getId().toString()).stream()
            .findFirst()
            .orElseThrow();
    htmlPost(
        "/admin/courses/assignments/" + assignment.getId() + "/delete",
        token,
        form("courseId", course.getId().toString()));

    htmlPost("/admin/groups/" + group.getId() + "/delete", token, form());
    htmlPost("/admin/promotions/" + createdPromotion.getId() + "/delete", token, form());
    htmlPost("/admin/courses/" + course.getId() + "/delete", token, form());
  }

  @Test
  void admin_can_manage_teachers_and_students() {
    String token = adminJwt();
    Promotion promotion = newPromotion();
    Group group = newGroup(promotion);

    String teacherNumber = "TW" + uid();
    htmlPost(
        "/admin/teachers/save",
        token,
        form(
            "firstName",
            "Paul",
            "lastName",
            "Rabe",
            "email",
            email(teacherNumber),
            "employeeNumber",
            teacherNumber,
            "password",
            PASSWORD,
            "enabled",
            "true"));
    Teacher teacher =
        teacherService.listAll().stream()
            .filter(item -> teacherNumber.equals(item.getEmployeeNumber()))
            .findFirst()
            .orElseThrow();
    assertContains(
        htmlGet("/admin/teachers/profile.html?id=" + teacher.getId(), token).getBody(), "Paul");

    htmlPost(
        "/admin/teachers/" + teacher.getId() + "/profile",
        token,
        form(
            "firstName",
            "Paul",
            "lastName",
            "Modifié",
            "email",
            teacher.getUser().getEmail(),
            "employeeNumber",
            teacherNumber,
            "enabled",
            "true"));
    assertContains(
        htmlGet("/admin/teachers/form.html?id=" + teacher.getId(), token).getBody(), "Modifié");

    String studentNumber = "SW" + uid();
    htmlPost(
        "/admin/students/save",
        token,
        form(
            "firstName",
            "Lala",
            "lastName",
            "Rabe",
            "email",
            email(studentNumber),
            "studentNumber",
            studentNumber,
            "promotionId",
            promotion.getId().toString(),
            "groupId",
            group.getId().toString(),
            "password",
            PASSWORD,
            "enabled",
            "true"));
    Student student =
        studentService.listAll().stream()
            .filter(item -> studentNumber.equals(item.getStudentNumber()))
            .findFirst()
            .orElseThrow();
    assertContains(
        htmlGet("/admin/students/profile.html?id=" + student.getId(), token).getBody(), "Lala");

    htmlPost(
        "/admin/students/" + student.getId() + "/profile",
        token,
        form(
            "firstName",
            "Lala",
            "lastName",
            "Profil",
            "email",
            student.getUser().getEmail(),
            "studentNumber",
            studentNumber));
    htmlPost("/admin/students/" + student.getId() + "/suspend", token, form("redirect", "profile"));
    assertContains(
        htmlGet("/admin/students/profile.html?id=" + student.getId(), token).getBody(), "Suspendu");
    htmlPost(
        "/admin/students/" + student.getId() + "/unsuspend", token, form("redirect", "profile"));

    Group otherGroup = newGroup(promotion);
    assertContains(
        htmlGet("/admin/students/edit-group.html?id=" + student.getId(), token).getBody(),
        otherGroup.getRef());
    htmlPost(
        "/admin/students/" + student.getId() + "/group",
        token,
        form("groupId", otherGroup.getId().toString()));

    htmlPost("/admin/teachers/" + teacher.getId() + "/delete", token, form());
    htmlPost("/admin/students/" + student.getId() + "/delete", token, form());
  }

  @Test
  void admin_can_manage_grades_and_graduates() {
    String token = adminJwt();
    Promotion promotion = newPromotion();
    Group group = newGroup(promotion);
    Course course = newCourse();
    Teacher teacher = newTeacher();
    Student graduate = newStudent(promotion, group);
    Student failing = newStudent(promotion, group);
    assign(course, teacher, group);

    ExamOption exam =
        teacherGradeService.createExam(
            teacher.getId().toString(),
            course.getId().toString(),
            Instant.parse("2026-06-01T08:00:00Z"),
            BigDecimal.ONE,
            List.of(group.getId().toString()));
    teacherGradeService.saveGrade(
        teacher.getId().toString(),
        exam.examId(),
        graduate.getId().toString(),
        new BigDecimal("14.00"));
    teacherGradeService.saveGrade(
        teacher.getId().toString(),
        exam.examId(),
        failing.getId().toString(),
        new BigDecimal("6.00"));

    assertContains(htmlGet("/admin/grades/home.html", token).getBody(), course.getRef());
    assertContains(
        htmlGet("/admin/grades/select-exam.html?courseId=" + course.getId(), token).getBody(),
        "Examens disponibles");
    assertContains(
        htmlGet(
                "/admin/grades/select-group.html?examId="
                    + exam.examId()
                    + "&courseId="
                    + course.getId(),
                token)
            .getBody(),
        group.getRef());
    assertContains(
        htmlGet(
                "/admin/grades/edit.html?examId="
                    + exam.examId()
                    + "&groupId="
                    + group.getId()
                    + "&courseId="
                    + course.getId(),
                token)
            .getBody(),
        graduate.getUser().getFullName());

    htmlPost(
        "/admin/grades/save",
        token,
        form(
            "examId",
            exam.examId(),
            "groupId",
            group.getId().toString(),
            "courseId",
            course.getId().toString(),
            "studentId",
            failing.getId().toString(),
            "value",
            "8.00",
            "reason",
            "Correction admin"));

    var gradeSheet = adminGradeService.getGradeSheet(exam.examId(), group.getId().toString());
    String gradeId =
        gradeSheet.rows().stream()
            .filter(row -> failing.getId().toString().equals(row.studentId()))
            .findFirst()
            .orElseThrow()
            .gradeId();
    assertContains(
        htmlGet(
                "/admin/grades/grade-history.html?gradeId="
                    + gradeId
                    + "&examId="
                    + exam.examId()
                    + "&groupId="
                    + group.getId()
                    + "&courseId="
                    + course.getId(),
                token)
            .getBody(),
        "Historique de note");

    teacherGradeService.submitExam(teacher.getId().toString(), exam.examId());

    assertContains(
        htmlGet("/admin/promotion/results.html?promotionId=" + promotion.getId(), token).getBody(),
        graduate.getStudentNumber());
    var exportResponse =
        restTemplate.exchange(
            "/admin/promotion/graduates.xlsx?promotionId=" + promotion.getId(),
            HttpMethod.GET,
            new HttpEntity<>(cookieHeaders(token)),
            byte[].class);
    assertEquals(HttpStatus.OK, exportResponse.getStatusCode());
    assertTrue(exportResponse.getBody() != null && exportResponse.getBody().length > 0);
  }

  private HttpHeaders cookieHeaders(String token) {
    HttpHeaders headers = new HttpHeaders();
    headers.add(HttpHeaders.COOKIE, "jwt=" + token);
    return headers;
  }
}
