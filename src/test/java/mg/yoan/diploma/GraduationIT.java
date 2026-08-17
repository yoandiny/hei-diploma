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
import mg.yoan.diploma.service.GraduationService;
import mg.yoan.diploma.service.TeacherGradeService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class GraduationIT extends DiplomaIT {

  @Autowired private TeacherGradeService teacherGradeService;
  @Autowired private GraduationService graduationService;

  @Test
  void lists_only_students_with_every_required_course_at_least_ten() {
    Promotion promotion = newPromotion();
    Group group = newGroup(promotion);
    Course course = newCourse();
    Teacher teacher = newTeacher();
    Student passing = newStudent(promotion, group);
    Student failing = newStudent(promotion, group);
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
        passing.getId().toString(),
        new BigDecimal("12.00"));
    teacherGradeService.saveGrade(
        teacher.getId().toString(),
        exam.examId(),
        failing.getId().toString(),
        new BigDecimal("8.00"));
    teacherGradeService.submitExam(teacher.getId().toString(), exam.examId());

    var graduates = graduationService.listGraduates(promotion.getId().toString());
    assertEquals(1, graduates.size());
    assertEquals(passing.getId(), graduates.get(0).student().getId());
    assertTrue(graduates.get(0).weightedAverage().compareTo(new BigDecimal("10")) >= 0);
  }
}
