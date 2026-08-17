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
import mg.yoan.diploma.service.AdminDashboardService;
import mg.yoan.diploma.service.TeacherGradeService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class AdminDashboardIT extends DiplomaIT {

  @Autowired private AdminDashboardService adminDashboardService;
  @Autowired private TeacherGradeService teacherGradeService;

  @Test
  void home_counts_created_entities_and_grade_changes() {
    var before = adminDashboardService.loadHome();
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
        new BigDecimal("13.50"));

    var after = adminDashboardService.loadHome();
    assertTrue(after.courseCount() >= before.courseCount() + 1);
    assertTrue(after.studentCount() >= before.studentCount() + 1);
    assertTrue(after.teacherCount() >= before.teacherCount() + 1);
    assertTrue(after.gradeChangeCount() >= before.gradeChangeCount() + 1);
    assertTrue(
        after.recentChanges().stream()
            .anyMatch(change -> student.getStudentNumber().equals(change.studentNumber())));
    assertEquals(
        new BigDecimal("13.50"),
        adminDashboardService.listGradeChanges().stream()
            .filter(change -> student.getStudentNumber().equals(change.studentNumber()))
            .findFirst()
            .orElseThrow()
            .newValue());
  }
}
