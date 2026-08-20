package mg.yoan.diploma;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import mg.yoan.diploma.domain.Course;
import mg.yoan.diploma.domain.Group;
import mg.yoan.diploma.domain.Promotion;
import mg.yoan.diploma.domain.Student;
import mg.yoan.diploma.domain.Teacher;
import mg.yoan.diploma.service.TeacherGradeService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

class StudentWebIT extends WebDiplomaIT {

  @Autowired private TeacherGradeService teacherGradeService;

  @Test
  void student_can_open_pages_with_grades() {
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

    String token = studentJwt(student);

    assertContains(htmlGet("/student/dashboard.html", token).getBody(), "Dashboard");
    assertContains(htmlGet("/student/profile.html", token).getBody(), "Mon profil");
    assertContains(htmlGet("/student/grades.html", token).getBody(), course.getRef());

    var redirect = htmlGet("/student/transcripts.html", token);
    assertTrue(
        redirect.getStatusCode() == HttpStatus.FOUND
            || (redirect.getBody() != null && redirect.getBody().contains("Mes notes")));
  }
}
