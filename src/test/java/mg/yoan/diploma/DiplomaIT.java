package mg.yoan.diploma;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import mg.yoan.diploma.conf.FacadeIT;
import mg.yoan.diploma.domain.Course;
import mg.yoan.diploma.domain.Group;
import mg.yoan.diploma.domain.Promotion;
import mg.yoan.diploma.domain.Role;
import mg.yoan.diploma.domain.Student;
import mg.yoan.diploma.domain.Teacher;
import mg.yoan.diploma.repository.JUserRepository;
import mg.yoan.diploma.repository.model.JUser;
import mg.yoan.diploma.service.CourseAssignmentService;
import mg.yoan.diploma.service.CourseService;
import mg.yoan.diploma.service.GroupService;
import mg.yoan.diploma.service.PromotionService;
import mg.yoan.diploma.service.StudentService;
import mg.yoan.diploma.service.TeacherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;

public abstract class DiplomaIT extends FacadeIT {

  protected static final String PASSWORD = "Password1";

  @Autowired protected PromotionService promotionService;
  @Autowired protected GroupService groupService;
  @Autowired protected CourseService courseService;
  @Autowired protected TeacherService teacherService;
  @Autowired protected StudentService studentService;
  @Autowired protected CourseAssignmentService assignmentService;
  @Autowired protected JUserRepository userRepository;
  @Autowired protected PasswordEncoder passwordEncoder;

  protected String uid() {
    return UUID.randomUUID().toString().substring(0, 8);
  }

  protected Promotion newPromotion() {
    String label = "P" + uid();
    return promotionService.save(null, label, 2024, 2027);
  }

  protected Group newGroup(Promotion promotion) {
    return groupService.save(null, "G" + uid(), promotion.getId().toString());
  }

  protected Course newCourse() {
    String ref = "C" + uid();
    return courseService.save(null, ref, "Cours " + ref, 6);
  }

  protected Teacher newTeacher() {
    String number = "T" + uid();
    return teacherService.create("Jean", "Test", email(number), number, PASSWORD, true);
  }

  protected Student newStudent(Promotion promotion, Group group) {
    String number = "S" + uid();
    return studentService.create(
        "Aina",
        "Rabe",
        email(number),
        number,
        promotion.getId().toString(),
        group.getId().toString(),
        PASSWORD,
        true);
  }

  protected JUser newAdmin() {
    String number = "A" + uid();
    JUser user = new JUser();
    user.setId(UUID.randomUUID().toString());
    user.setEmail(email(number));
    user.setPassword(passwordEncoder.encode(PASSWORD));
    user.setFirstName("Ada");
    user.setLastName("Admin");
    user.setRole(Role.ADMIN);
    user.setEnabled(true);
    user.setCreatedAt(Instant.now());
    return userRepository.save(user);
  }

  protected String email(String localPart) {
    return localPart.toLowerCase() + "@mail.hei.school";
  }

  protected void assign(Course course, Teacher teacher, Group group) {
    assignmentService.assign(
        course.getId().toString(), teacher.getId().toString(), List.of(group.getId().toString()));
  }
}
