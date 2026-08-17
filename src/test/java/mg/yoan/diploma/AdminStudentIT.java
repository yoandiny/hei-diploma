package mg.yoan.diploma;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import mg.yoan.diploma.domain.Group;
import mg.yoan.diploma.domain.Promotion;
import mg.yoan.diploma.domain.Student;
import mg.yoan.diploma.service.DomainException;
import org.junit.jupiter.api.Test;

class AdminStudentIT extends DiplomaIT {

  @Test
  void create_lists_the_student() {
    Promotion promotion = newPromotion();
    Group group = newGroup(promotion);

    Student student = newStudent(promotion, group);

    assertTrue(
        studentService.listAll().stream().anyMatch(item -> item.getId().equals(student.getId())));
    assertEquals(group.getId(), student.getCurrentGroup().getId());
  }

  @Test
  void change_group_updates_current_group() {
    Promotion promotion = newPromotion();
    Group first = newGroup(promotion);
    Group second = newGroup(promotion);
    Student student = newStudent(promotion, first);

    Student updated =
        studentService.changeGroup(student.getId().toString(), second.getId().toString());

    assertEquals(second.getId(), updated.getCurrentGroup().getId());
  }

  @Test
  void change_group_rejects_a_group_from_another_promotion() {
    Promotion firstPromotion = newPromotion();
    Promotion secondPromotion = newPromotion();
    Group ownGroup = newGroup(firstPromotion);
    Group otherGroup = newGroup(secondPromotion);
    Student student = newStudent(firstPromotion, ownGroup);

    assertThrows(
        DomainException.class,
        () ->
            studentService.changeGroup(student.getId().toString(), otherGroup.getId().toString()));
  }

  @Test
  void suspend_disables_the_account() {
    Promotion promotion = newPromotion();
    Student student = newStudent(promotion, newGroup(promotion));

    studentService.suspend(student.getId().toString());

    assertFalse(studentService.getById(student.getId().toString()).getUser().isEnabled());
  }

  @Test
  void soft_delete_hides_the_student() {
    Promotion promotion = newPromotion();
    Student student = newStudent(promotion, newGroup(promotion));

    studentService.softDelete(student.getId().toString());

    assertTrue(
        studentService.listAll().stream().noneMatch(item -> item.getId().equals(student.getId())));
    assertThrows(DomainException.class, () -> studentService.getById(student.getId().toString()));
  }

  @Test
  void create_rejects_duplicate_email() {
    Promotion promotion = newPromotion();
    Group group = newGroup(promotion);
    Student student = newStudent(promotion, group);

    assertThrows(
        DomainException.class,
        () ->
            studentService.create(
                "Other",
                "Name",
                student.getUser().getEmail(),
                "S" + uid(),
                promotion.getId().toString(),
                group.getId().toString(),
                PASSWORD,
                true));
  }
}
