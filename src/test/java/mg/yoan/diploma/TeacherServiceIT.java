package mg.yoan.diploma;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import mg.yoan.diploma.domain.Course;
import mg.yoan.diploma.domain.Group;
import mg.yoan.diploma.domain.Promotion;
import mg.yoan.diploma.domain.Teacher;
import mg.yoan.diploma.service.DomainException;
import org.junit.jupiter.api.Test;

class TeacherServiceIT extends DiplomaIT {

  @Test
  void create_then_update() {
    Teacher teacher = newTeacher();

    Teacher updated =
        teacherService.update(
            teacher.getId().toString(),
            "Nouveau",
            "Nom",
            teacher.getUser().getEmail(),
            teacher.getEmployeeNumber(),
            null,
            true);

    assertEquals("Nouveau", updated.getUser().getFirstName());
  }

  @Test
  void create_rejects_duplicate_email() {
    Teacher existing = newTeacher();

    assertThrows(
        DomainException.class,
        () ->
            teacherService.create(
                "Jean", "Autre", existing.getUser().getEmail(), "T" + uid(), PASSWORD, true));
  }

  @Test
  void create_rejects_duplicate_employee_number() {
    Teacher existing = newTeacher();

    assertThrows(
        DomainException.class,
        () ->
            teacherService.create(
                "Jean", "Autre", email("t" + uid()), existing.getEmployeeNumber(), PASSWORD, true));
  }

  @Test
  void create_rejects_blank_password() {
    assertThrows(
        DomainException.class,
        () -> teacherService.create("Jean", "Test", email("t" + uid()), "T" + uid(), "  ", true));
  }

  @Test
  void update_rejects_email_already_used_by_another_teacher() {
    Teacher first = newTeacher();
    Teacher second = newTeacher();

    assertThrows(
        DomainException.class,
        () ->
            teacherService.update(
                second.getId().toString(),
                second.getUser().getFirstName(),
                second.getUser().getLastName(),
                first.getUser().getEmail(),
                second.getEmployeeNumber(),
                null,
                true));
  }

  @Test
  void delete_removes_unassigned_teacher() {
    Teacher teacher = newTeacher();

    teacherService.delete(teacher.getId().toString());

    assertTrue(teacherService.listAll().stream().noneMatch(t -> t.getId().equals(teacher.getId())));
  }

  @Test
  void delete_rejects_teacher_still_assigned_to_a_course() {
    Promotion promotion = newPromotion();
    Group group = newGroup(promotion);
    Course course = newCourse();
    Teacher teacher = newTeacher();
    assign(course, teacher, group);

    assertThrows(DomainException.class, () -> teacherService.delete(teacher.getId().toString()));
  }
}
