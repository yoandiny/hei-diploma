package mg.yoan.diploma;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import mg.yoan.diploma.domain.Teacher;
import mg.yoan.diploma.service.DomainException;
import org.junit.jupiter.api.Test;

class AdminTeacherIT extends DiplomaIT {

  @Test
  void update_changes_identity_fields() {
    Teacher teacher = newTeacher();
    String newEmail = email("tch" + uid());
    String newNumber = "T" + uid();

    Teacher updated =
        teacherService.update(
            teacher.getId().toString(), "Hery", "Rakoto", newEmail, newNumber, null, true);

    assertEquals("Hery", updated.getUser().getFirstName());
    assertEquals("Rakoto", updated.getUser().getLastName());
    assertEquals(newEmail, updated.getUser().getEmail());
    assertEquals(newNumber, updated.getEmployeeNumber());
    assertTrue(updated.getUser().isEnabled());
    assertTrue(
        passwordEncoder.matches(
            PASSWORD,
            userRepository.findById(teacher.getId().toString()).orElseThrow().getPassword()));
  }

  @Test
  void update_can_disable_the_account() {
    Teacher teacher = newTeacher();

    Teacher updated =
        teacherService.update(
            teacher.getId().toString(),
            teacher.getUser().getFirstName(),
            teacher.getUser().getLastName(),
            teacher.getUser().getEmail(),
            teacher.getEmployeeNumber(),
            null,
            false);

    assertFalse(updated.getUser().isEnabled());
  }

  @Test
  void update_rejects_duplicate_email() {
    Teacher first = newTeacher();
    Teacher second = newTeacher();

    assertThrows(
        DomainException.class,
        () ->
            teacherService.update(
                second.getId().toString(),
                "Hery",
                "Rakoto",
                first.getUser().getEmail(),
                second.getEmployeeNumber(),
                null,
                true));
  }
}
