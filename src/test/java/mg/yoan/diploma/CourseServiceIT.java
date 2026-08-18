package mg.yoan.diploma;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.util.List;
import mg.yoan.diploma.domain.Course;
import mg.yoan.diploma.domain.Group;
import mg.yoan.diploma.domain.Promotion;
import mg.yoan.diploma.domain.Teacher;
import mg.yoan.diploma.service.DomainException;
import mg.yoan.diploma.service.TeacherGradeService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class CourseServiceIT extends DiplomaIT {

  @Autowired private TeacherGradeService teacherGradeService;

  @Test
  void save_creates_then_updates() {
    Course created = newCourse();

    Course updated =
        courseService.save(created.getId().toString(), created.getRef(), "Nouveau titre", 8);

    assertEquals("Nouveau titre", updated.getTitle());
    assertEquals(8, updated.getCredits());
    assertEquals(created.getId(), updated.getId());
  }

  @Test
  void save_rejects_duplicate_ref() {
    Course existing = newCourse();

    assertThrows(
        DomainException.class, () -> courseService.save(null, existing.getRef(), "Autre titre", 4));
  }

  @Test
  void save_rejects_duplicate_ref_case_insensitive() {
    Course existing = newCourse();

    assertThrows(
        DomainException.class,
        () -> courseService.save(null, existing.getRef().toLowerCase(), "Autre titre", 4));
  }

  @Test
  void save_rejects_negative_credits() {
    assertThrows(DomainException.class, () -> courseService.save(null, "C" + uid(), "Titre", -1));
  }

  @Test
  void save_rejects_blank_ref() {
    assertThrows(DomainException.class, () -> courseService.save(null, "  ", "Titre", 4));
  }

  @Test
  void delete_removes_unassigned_course() {
    Course course = newCourse();

    courseService.delete(course.getId().toString());

    assertTrue(courseService.listAll().stream().noneMatch(c -> c.getId().equals(course.getId())));
  }

  @Test
  void delete_rejects_course_still_assigned() {
    Promotion promotion = newPromotion();
    Group group = newGroup(promotion);
    Course course = newCourse();
    Teacher teacher = newTeacher();
    assign(course, teacher, group);

    assertThrows(DomainException.class, () -> courseService.delete(course.getId().toString()));
  }

  @Test
  void delete_rejects_course_with_exams() {
    Promotion promotion = newPromotion();
    Group group = newGroup(promotion);
    Course course = newCourse();
    Teacher teacher = newTeacher();
    assign(course, teacher, group);
    teacherGradeService.createExam(
        teacher.getId().toString(),
        course.getId().toString(),
        Instant.parse("2026-06-01T08:00:00Z"),
        java.math.BigDecimal.ONE,
        List.of(group.getId().toString()));

    assertThrows(DomainException.class, () -> courseService.delete(course.getId().toString()));
  }
}
