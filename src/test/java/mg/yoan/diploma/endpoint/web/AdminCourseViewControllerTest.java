package mg.yoan.diploma.endpoint.web;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.UUID;
import mg.yoan.diploma.domain.Course;
import mg.yoan.diploma.domain.CourseAssignment;
import mg.yoan.diploma.domain.Group;
import mg.yoan.diploma.domain.Teacher;
import org.junit.jupiter.api.Test;

class AdminCourseViewControllerTest {

  @Test
  void course_row_lists_each_teacher_and_group_once() {
    Teacher teacher = teacher("Giovanni");
    Group k1 = group("K1");
    Group k2 = group("K2");
    Group k3 = group("K3");
    var row =
        new AdminCourseViewController.CourseRow(
            Course.builder().id(UUID.randomUUID()).ref("LV2").title("Anglais").credits(6).build(),
            List.of(assignment(teacher, k1), assignment(teacher, k2), assignment(teacher, k3)));

    assertEquals(1, row.teachers().size());
    assertEquals(teacher.getId(), row.teachers().getFirst().getId());
    assertEquals(List.of("K1", "K2", "K3"), row.groups().stream().map(Group::getRef).toList());
  }

  private static Teacher teacher(String employeeNumber) {
    return Teacher.builder().id(UUID.randomUUID()).employeeNumber(employeeNumber).build();
  }

  private static Group group(String ref) {
    return Group.builder().id(UUID.randomUUID()).ref(ref).build();
  }

  private static CourseAssignment assignment(Teacher teacher, Group group) {
    return CourseAssignment.builder()
        .id(UUID.randomUUID())
        .teacher(teacher)
        .group(group)
        .build();
  }
}
