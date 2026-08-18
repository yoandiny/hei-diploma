package mg.yoan.diploma;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import mg.yoan.diploma.domain.Course;
import mg.yoan.diploma.domain.Group;
import mg.yoan.diploma.domain.Promotion;
import mg.yoan.diploma.domain.Teacher;
import mg.yoan.diploma.service.DomainException;
import org.junit.jupiter.api.Test;

class CourseAssignmentServiceIT extends DiplomaIT {

  @Test
  void assign_creates_one_assignment_per_group() {
    Promotion promotion = newPromotion();
    Group first = newGroup(promotion);
    Group second = newGroup(promotion);
    Course course = newCourse();
    Teacher teacher = newTeacher();

    assignmentService.assign(
        course.getId().toString(),
        teacher.getId().toString(),
        List.of(first.getId().toString(), second.getId().toString()));

    assertEquals(2, assignmentService.listByCourse(course.getId().toString()).size());
  }

  @Test
  void assign_rejects_empty_group_list() {
    Course course = newCourse();
    Teacher teacher = newTeacher();

    assertThrows(
        DomainException.class,
        () ->
            assignmentService.assign(
                course.getId().toString(), teacher.getId().toString(), List.of()));
  }

  @Test
  void assign_rejects_re_assigning_the_exact_same_group() {
    Promotion promotion = newPromotion();
    Group group = newGroup(promotion);
    Course course = newCourse();
    Teacher teacher = newTeacher();
    assign(course, teacher, group);

    assertThrows(
        DomainException.class,
        () ->
            assignmentService.assign(
                course.getId().toString(),
                teacher.getId().toString(),
                List.of(group.getId().toString())));
    assertEquals(1, assignmentService.listByCourse(course.getId().toString()).size());
  }

  @Test
  void assign_creates_only_the_new_group_when_batch_mixes_new_and_existing() {
    Promotion promotion = newPromotion();
    Group already = newGroup(promotion);
    Group fresh = newGroup(promotion);
    Course course = newCourse();
    Teacher teacher = newTeacher();
    assign(course, teacher, already);

    assignmentService.assign(
        course.getId().toString(),
        teacher.getId().toString(),
        List.of(already.getId().toString(), fresh.getId().toString()));

    assertEquals(2, assignmentService.listByCourse(course.getId().toString()).size());
  }

  @Test
  void delete_removes_the_assignment() {
    Promotion promotion = newPromotion();
    Group group = newGroup(promotion);
    Course course = newCourse();
    Teacher teacher = newTeacher();
    assign(course, teacher, group);
    String assignmentId =
        assignmentService.listByCourse(course.getId().toString()).get(0).getId().toString();

    assignmentService.delete(assignmentId);

    assertTrue(assignmentService.listByCourse(course.getId().toString()).isEmpty());
  }

  @Test
  void delete_rejects_unknown_assignment() {
    assertThrows(DomainException.class, () -> assignmentService.delete("not-a-real-id"));
  }
}
