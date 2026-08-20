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
import mg.yoan.diploma.service.TeacherGradeService;
import mg.yoan.diploma.service.TeacherGradeService.ExamOption;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

class TeacherWebIT extends WebDiplomaIT {

  @Autowired private TeacherGradeService teacherGradeService;

  @Test
  void teacher_can_open_workspace_pages() {
    Promotion promotion = newPromotion();
    Group group = newGroup(promotion);
    Course course = newCourse();
    Teacher teacher = newTeacher();
    assign(course, teacher, group);
    String token = teacherJwt(teacher);

    assertContains(htmlGet("/teacher/dashboard.html", token).getBody(), "Dashboard enseignant");
    assertContains(htmlGet("/teacher/courses.html", token).getBody(), course.getRef());
    assertContains(htmlGet("/teacher/exams.html", token).getBody(), "Mes examens");
    assertContains(htmlGet("/teacher/exams-new.html", token).getBody(), "Nouvel examen");
    assertContains(
        htmlGet("/teacher/exams-new.html?courseId=" + course.getId(), token).getBody(),
        course.getRef());
    assertContains(
        htmlGet("/teacher/grades-home.html", token).getBody(), "Notes de mes cours");
    assertContains(
        htmlGet("/teacher/exams.html?courseId=" + course.getId(), token).getBody(),
        "Mes examens");
  }

  @Test
  void teacher_can_create_exam_save_grade_submit_and_view_history() {
    Promotion promotion = newPromotion();
    Group group = newGroup(promotion);
    Course course = newCourse();
    Teacher teacher = newTeacher();
    Student student = newStudent(promotion, group);
    assign(course, teacher, group);
    String token = teacherJwt(teacher);

    MultiValueMap<String, String> createExam = new LinkedMultiValueMap<>();
    createExam.add("courseId", course.getId().toString());
    createExam.add("dateExam", "2026-06-01T08:00:00");
    createExam.add("coefficient", "1");
    createExam.add("groupIds", group.getId().toString());
    htmlPost("/teacher/exams", token, createExam);

    ExamOption exam =
        teacherGradeService.listTeacherExams(teacher.getId().toString()).stream()
            .filter(item -> course.getId().toString().equals(item.courseId()))
            .findFirst()
            .orElseThrow();

    assertContains(
        htmlGet("/teacher/exams-detail.html?examId=" + exam.examId(), token).getBody(),
        course.getRef());

    htmlPost(
        "/teacher/grades",
        token,
        form(
            "examId",
            exam.examId(),
            "groupId",
            group.getId().toString(),
            "studentId",
            student.getId().toString(),
            "value",
            "11.00"));

    var sheet = teacherGradeService.getGradeSheet(teacher.getId().toString(), exam.examId(), null);
    String gradeId = sheet.rows().get(0).gradeId();

    assertContains(
        htmlGet(
                "/teacher/grades-edit.html?examId="
                    + exam.examId()
                    + "&groupId="
                    + group.getId(),
                token)
            .getBody(),
        student.getUser().getFullName());

    htmlPost(
        "/teacher/grades",
        token,
        form(
            "examId",
            exam.examId(),
            "groupId",
            group.getId().toString(),
            "studentId",
            student.getId().toString(),
            "value",
            "13.00",
            "reason",
            "Erreur de saisie"));

    htmlPost("/teacher/exams/submit", token, form("examId", exam.examId()));

    ExamOption submitted = teacherGradeService.getExam(teacher.getId().toString(), exam.examId());
    assertTrue(submitted.isSubmitted());
    assertEquals(
        2,
        teacherGradeService.getHistory(teacher.getId().toString(), gradeId).entries().size());
  }
}
