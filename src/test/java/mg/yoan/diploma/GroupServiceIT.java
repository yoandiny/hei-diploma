package mg.yoan.diploma;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import mg.yoan.diploma.domain.Course;
import mg.yoan.diploma.domain.Group;
import mg.yoan.diploma.domain.Promotion;
import mg.yoan.diploma.domain.Student;
import mg.yoan.diploma.domain.Teacher;
import mg.yoan.diploma.service.DomainException;
import org.junit.jupiter.api.Test;

class GroupServiceIT extends DiplomaIT {

  @Test
  void save_creates_then_updates() {
    Promotion promotion = newPromotion();
    Group group = newGroup(promotion);

    Group updated =
        groupService.save(group.getId().toString(), "NEWREF" + uid(), promotion.getId().toString());

    assertEquals(group.getId(), updated.getId());
  }

  @Test
  void save_rejects_duplicate_ref_within_same_promotion() {
    Promotion promotion = newPromotion();
    Group group = newGroup(promotion);

    assertThrows(
        DomainException.class,
        () -> groupService.save(null, group.getRef(), promotion.getId().toString()));
  }

  @Test
  void save_allows_same_ref_across_different_promotions() {
    Promotion first = newPromotion();
    Promotion second = newPromotion();
    Group group = newGroup(first);

    Group other = groupService.save(null, group.getRef(), second.getId().toString());

    assertEquals(group.getRef(), other.getRef());
  }

  @Test
  void save_rejects_unknown_promotion() {
    assertThrows(
        DomainException.class, () -> groupService.save(null, "G" + uid(), "not-a-real-id"));
  }

  @Test
  void delete_removes_empty_unassigned_group() {
    Promotion promotion = newPromotion();
    Group group = newGroup(promotion);

    groupService.delete(group.getId().toString());

    assertTrue(groupService.listAll().stream().noneMatch(g -> g.getId().equals(group.getId())));
  }

  @Test
  void delete_rejects_group_with_students() {
    Promotion promotion = newPromotion();
    Group group = newGroup(promotion);
    newStudent(promotion, group);

    assertThrows(DomainException.class, () -> groupService.delete(group.getId().toString()));
  }

  @Test
  void delete_rejects_group_still_assigned_to_a_course() {
    Promotion promotion = newPromotion();
    Group group = newGroup(promotion);
    Course course = newCourse();
    Teacher teacher = newTeacher();
    assign(course, teacher, group);

    assertThrows(DomainException.class, () -> groupService.delete(group.getId().toString()));
  }

  @Test
  void delete_rejects_group_present_in_student_group_history() {
    Promotion promotion = newPromotion();
    Group first = newGroup(promotion);
    Group second = newGroup(promotion);
    Student student = newStudent(promotion, first);
    studentService.changeGroup(student.getId().toString(), second.getId().toString());

    assertThrows(DomainException.class, () -> groupService.delete(first.getId().toString()));
  }
}
