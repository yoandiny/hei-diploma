package mg.yoan.diploma;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import mg.yoan.diploma.domain.Course;
import mg.yoan.diploma.domain.Group;
import mg.yoan.diploma.domain.Promotion;
import mg.yoan.diploma.domain.Student;
import mg.yoan.diploma.domain.Teacher;
import mg.yoan.diploma.repository.model.JUser;
import mg.yoan.diploma.service.AdminGradeService;
import mg.yoan.diploma.service.DomainException;
import mg.yoan.diploma.service.TeacherGradeService;
import mg.yoan.diploma.service.TeacherGradeService.ExamOption;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class AdminGradeIT extends DiplomaIT {

  @Autowired private AdminGradeService adminGradeService;
  @Autowired private TeacherGradeService teacherGradeService;

  @Test
  void admin_can_create_a_grade_without_a_reason() {
    var fixture = workspace();

    adminGradeService.saveGrade(
        fixture.adminId(), fixture.examId(), fixture.studentId(), new BigDecimal("12.00"));

    var sheet = adminGradeService.getGradeSheet(fixture.examId(), fixture.groupId());
    assertEquals(new BigDecimal("12.00"), sheet.rows().get(0).currentValue());
  }

  @Test
  void admin_correcting_a_grade_requires_a_reason() {
    var fixture = workspace();
    adminGradeService.saveGrade(
        fixture.adminId(), fixture.examId(), fixture.studentId(), new BigDecimal("12.00"));

    DomainException exception =
        assertThrows(
            DomainException.class,
            () ->
                adminGradeService.saveGrade(
                    fixture.adminId(),
                    fixture.examId(),
                    fixture.studentId(),
                    new BigDecimal("15.00")));
    assertEquals("Un motif est obligatoire pour modifier une note.", exception.getMessage());
  }

  @Test
  void admin_correction_with_a_reason_is_recorded_in_history() {
    var fixture = workspace();
    adminGradeService.saveGrade(
        fixture.adminId(), fixture.examId(), fixture.studentId(), new BigDecimal("12.00"));

    adminGradeService.saveGrade(
        fixture.adminId(),
        fixture.examId(),
        fixture.studentId(),
        new BigDecimal("15.00"),
        "Réclamation étudiant acceptée");

    String gradeId =
        adminGradeService
            .getGradeSheet(fixture.examId(), fixture.groupId())
            .rows()
            .get(0)
            .gradeId();
    var history = adminGradeService.getHistory(gradeId);
    assertEquals(2, history.entries().size());
    var latest = history.entries().get(0);
    assertEquals(new BigDecimal("12.00"), latest.previousValue());
    assertEquals(new BigDecimal("15.00"), latest.newValue());
    assertEquals("Réclamation étudiant acceptée", latest.reason());
  }

  @Test
  void saving_the_same_value_again_is_rejected() {
    var fixture = workspace();
    adminGradeService.saveGrade(
        fixture.adminId(), fixture.examId(), fixture.studentId(), new BigDecimal("12.00"));

    assertThrows(
        DomainException.class,
        () ->
            adminGradeService.saveGrade(
                fixture.adminId(),
                fixture.examId(),
                fixture.studentId(),
                new BigDecimal("12.00"),
                "Motif quelconque"));
  }

  @Test
  void admin_cannot_grade_beyond_the_exam_max_grade() {
    var fixture = workspace();

    assertThrows(
        DomainException.class,
        () ->
            adminGradeService.saveGrade(
                fixture.adminId(), fixture.examId(), fixture.studentId(), new BigDecimal("21.00")));
  }

  @Test
  void admin_cannot_grade_a_student_not_covered_by_the_exam() {
    Promotion promotion = newPromotion();
    Group examGroup = newGroup(promotion);
    Group otherGroup = newGroup(promotion);
    Course course = newCourse();
    Teacher teacher = newTeacher();
    Student outsider = newStudent(promotion, otherGroup);
    assign(course, teacher, examGroup);
    assign(course, teacher, otherGroup);
    ExamOption exam =
        teacherGradeService.createExam(
            teacher.getId().toString(),
            course.getId().toString(),
            Instant.parse("2026-06-01T08:00:00Z"),
            BigDecimal.ONE,
            List.of(examGroup.getId().toString()));
    JUser admin = newAdmin();

    assertThrows(
        DomainException.class,
        () ->
            adminGradeService.saveGrade(
                admin.getId(),
                exam.examId(),
                outsider.getId().toString(),
                new BigDecimal("10.00")));
  }

  @Test
  void admin_cannot_grade_an_already_submitted_exam() {
    var fixture = workspace();
    teacherGradeService.saveGrade(
        fixture.teacherId(), fixture.examId(), fixture.studentId(), new BigDecimal("11.00"));
    teacherGradeService.submitExam(fixture.teacherId(), fixture.examId());

    assertThrows(
        DomainException.class,
        () ->
            adminGradeService.saveGrade(
                fixture.adminId(), fixture.examId(), fixture.studentId(), new BigDecimal("14.00")));
  }

  private Workspace workspace() {
    Promotion promotion = newPromotion();
    Group group = newGroup(promotion);
    Course course = newCourse();
    Teacher teacher = newTeacher();
    Student student = newStudent(promotion, group);
    assign(course, teacher, group);
    ExamOption exam =
        teacherGradeService.createExam(
            teacher.getId().toString(),
            course.getId().toString(),
            Instant.parse("2026-06-01T08:00:00Z"),
            BigDecimal.ONE,
            List.of(group.getId().toString()));
    JUser admin = newAdmin();
    return new Workspace(
        admin.getId(),
        teacher.getId().toString(),
        exam.examId(),
        group.getId().toString(),
        student.getId().toString());
  }

  private record Workspace(
      String adminId, String teacherId, String examId, String groupId, String studentId) {}
}
