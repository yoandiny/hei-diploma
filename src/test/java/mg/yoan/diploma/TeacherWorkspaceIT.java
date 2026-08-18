package mg.yoan.diploma;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import mg.yoan.diploma.domain.Course;
import mg.yoan.diploma.domain.Group;
import mg.yoan.diploma.domain.Promotion;
import mg.yoan.diploma.domain.Student;
import mg.yoan.diploma.domain.Teacher;
import mg.yoan.diploma.service.DomainException;
import mg.yoan.diploma.service.TeacherGradeService;
import mg.yoan.diploma.service.TeacherGradeService.ExamOption;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class TeacherWorkspaceIT extends DiplomaIT {

  @Autowired private TeacherGradeService teacherGradeService;

  @Test
  void teacher_only_sees_assigned_courses() {
    Promotion promotion = newPromotion();
    Group group = newGroup(promotion);
    Course course = newCourse();
    Teacher owner = newTeacher();
    Teacher stranger = newTeacher();
    assign(course, owner, group);

    assertEquals(1, teacherGradeService.listAssignedCourses(owner.getId().toString()).size());
    assertTrue(teacherGradeService.listAssignedCourses(stranger.getId().toString()).isEmpty());
  }

  @Test
  void create_exam_and_grade_then_submit() {
    var fixture = workspace();
    ExamOption exam =
        teacherGradeService.createExam(
            fixture.teacherId(),
            fixture.courseId(),
            Instant.parse("2026-06-01T08:00:00Z"),
            BigDecimal.ONE,
            List.of(fixture.groupId()));

    teacherGradeService.saveGrade(
        fixture.teacherId(), exam.examId(), fixture.studentId(), new BigDecimal("14.00"));
    teacherGradeService.submitExam(fixture.teacherId(), exam.examId());

    ExamOption submitted = teacherGradeService.getExam(fixture.teacherId(), exam.examId());
    assertTrue(submitted.isSubmitted());
    assertEquals(1, submitted.gradedCount());
    assertThrows(
        DomainException.class,
        () ->
            teacherGradeService.saveGrade(
                fixture.teacherId(), exam.examId(), fixture.studentId(), new BigDecimal("15.00")));
  }

  @Test
  void teacher_cannot_open_exam_of_another_class() {
    Promotion promotion = newPromotion();
    Group groupA = newGroup(promotion);
    Group groupB = newGroup(promotion);
    Course course = newCourse();
    Teacher teacherA = newTeacher();
    Teacher teacherB = newTeacher();
    assign(course, teacherA, groupA);
    assign(course, teacherB, groupB);
    ExamOption exam =
        teacherGradeService.createExam(
            teacherA.getId().toString(),
            course.getId().toString(),
            Instant.parse("2026-06-01T08:00:00Z"),
            BigDecimal.ONE,
            List.of(groupA.getId().toString()));

    assertThrows(
        DomainException.class,
        () -> teacherGradeService.getExam(teacherB.getId().toString(), exam.examId()));
  }

  @Test
  void cannot_create_exam_for_unassigned_group() {
    Promotion promotion = newPromotion();
    Group assignedGroup = newGroup(promotion);
    Group otherGroup = newGroup(promotion);
    Course course = newCourse();
    Teacher teacher = newTeacher();
    assign(course, teacher, assignedGroup);

    assertThrows(
        DomainException.class,
        () ->
            teacherGradeService.createExam(
                teacher.getId().toString(),
                course.getId().toString(),
                Instant.parse("2026-06-01T08:00:00Z"),
                BigDecimal.ONE,
                List.of(otherGroup.getId().toString())));
  }

  @Test
  void grade_history_is_recorded() {
    var fixture = workspace();
    ExamOption exam =
        teacherGradeService.createExam(
            fixture.teacherId(),
            fixture.courseId(),
            Instant.parse("2026-06-01T08:00:00Z"),
            BigDecimal.ONE,
            List.of(fixture.groupId()));
    teacherGradeService.saveGrade(
        fixture.teacherId(), exam.examId(), fixture.studentId(), new BigDecimal("11.00"));
    var sheet = teacherGradeService.getGradeSheet(fixture.teacherId(), exam.examId(), null);
    String gradeId = sheet.rows().get(0).gradeId();

    var history = teacherGradeService.getHistory(fixture.teacherId(), gradeId);
    assertEquals(1, history.entries().size());
    assertEquals(new BigDecimal("11.00"), history.entries().get(0).newValue());
  }

  @Test
  void grade_update_requires_reason() {
    var fixture = workspace();
    ExamOption exam =
        teacherGradeService.createExam(
            fixture.teacherId(),
            fixture.courseId(),
            Instant.parse("2026-06-01T08:00:00Z"),
            BigDecimal.ONE,
            List.of(fixture.groupId()));
    teacherGradeService.saveGrade(
        fixture.teacherId(), exam.examId(), fixture.studentId(), new BigDecimal("11.00"));

    DomainException exception =
        assertThrows(
            DomainException.class,
            () ->
                teacherGradeService.saveGrade(
                    fixture.teacherId(),
                    exam.examId(),
                    fixture.studentId(),
                    new BigDecimal("13.00")));
    assertEquals("Un motif est obligatoire pour modifier une note.", exception.getMessage());
  }

  @Test
  void grade_update_with_reason_is_recorded() {
    var fixture = workspace();
    ExamOption exam =
        teacherGradeService.createExam(
            fixture.teacherId(),
            fixture.courseId(),
            Instant.parse("2026-06-01T08:00:00Z"),
            BigDecimal.ONE,
            List.of(fixture.groupId()));
    teacherGradeService.saveGrade(
        fixture.teacherId(), exam.examId(), fixture.studentId(), new BigDecimal("11.00"));
    var sheet = teacherGradeService.getGradeSheet(fixture.teacherId(), exam.examId(), null);
    String gradeId = sheet.rows().get(0).gradeId();

    teacherGradeService.updateGrade(
        fixture.teacherId(),
        exam.examId(),
        fixture.studentId(),
        new BigDecimal("13.00"),
        "Erreur de saisie");

    var history = teacherGradeService.getHistory(fixture.teacherId(), gradeId);
    assertEquals(2, history.entries().size());
    var latest = history.entries().get(0);
    assertEquals(new BigDecimal("11.00"), latest.previousValue());
    assertEquals(new BigDecimal("13.00"), latest.newValue());
    assertEquals("Erreur de saisie", latest.reason());
  }

  private Workspace workspace() {
    Promotion promotion = newPromotion();
    Group group = newGroup(promotion);
    Course course = newCourse();
    Teacher teacher = newTeacher();
    Student student = newStudent(promotion, group);
    assign(course, teacher, group);
    return new Workspace(
        teacher.getId().toString(),
        course.getId().toString(),
        group.getId().toString(),
        student.getId().toString());
  }

  private record Workspace(String teacherId, String courseId, String groupId, String studentId) {}
}
