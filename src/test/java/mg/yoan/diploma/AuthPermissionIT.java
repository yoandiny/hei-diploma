package mg.yoan.diploma;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import jakarta.servlet.http.Cookie;
import mg.yoan.diploma.domain.Role;
import mg.yoan.diploma.domain.Teacher;
import mg.yoan.diploma.endpoint.rest.security.JwtService;
import mg.yoan.diploma.repository.model.JUser;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

@AutoConfigureMockMvc
class AuthPermissionIT extends DiplomaIT {

  @Autowired private MockMvc mockMvc;
  @Autowired private JwtService jwtService;

  @Test
  void anonymous_is_sent_to_login() throws Exception {
    mockMvc.perform(get("/admin/dashboard.html")).andExpect(redirectedUrl("/login"));
    mockMvc.perform(get("/teacher/dashboard.html")).andExpect(redirectedUrl("/login"));
  }

  @Test
  void teacher_cannot_open_admin_pages() throws Exception {
    Teacher teacher = newTeacher();
    mockMvc
        .perform(
            get("/admin/dashboard.html")
                .cookie(jwtCookie(teacher.getId().toString(), Role.TEACHER)))
        .andExpect(redirectedUrl("/forbidden"));
  }

  @Test
  void admin_cannot_open_teacher_pages() throws Exception {
    JUser admin = newAdmin();
    mockMvc
        .perform(get("/teacher/dashboard.html").cookie(jwtCookie(admin.getId(), Role.ADMIN)))
        .andExpect(redirectedUrl("/forbidden"));
  }

  @Test
  void teacher_reaches_teacher_home() throws Exception {
    Teacher teacher = newTeacher();
    mockMvc
        .perform(
            get("/teacher/dashboard.html")
                .cookie(jwtCookie(teacher.getId().toString(), Role.TEACHER)))
        .andExpect(status().isOk());
  }

  @Test
  void admin_reaches_admin_home() throws Exception {
    JUser admin = newAdmin();
    mockMvc
        .perform(get("/admin/dashboard.html").cookie(jwtCookie(admin.getId(), Role.ADMIN)))
        .andExpect(status().isOk());
    mockMvc
        .perform(get("/admin/students/list.html").cookie(jwtCookie(admin.getId(), Role.ADMIN)))
        .andExpect(status().isOk());
  }

  @Test
  void disabled_teacher_token_is_rejected() throws Exception {
    Teacher teacher = newTeacher();
    var user = teacher.getUser();
    teacherService.update(
        teacher.getId().toString(),
        user.getFirstName(),
        user.getLastName(),
        user.getEmail(),
        teacher.getEmployeeNumber(),
        null,
        false);
    mockMvc
        .perform(
            get("/teacher/dashboard.html")
                .cookie(jwtCookie(teacher.getId().toString(), Role.TEACHER)))
        .andExpect(redirectedUrl("/login"));
  }

  private Cookie jwtCookie(String userId, Role role) {
    JUser user = userRepository.findById(userId).orElseThrow();
    String token =
        jwtService.generateToken(
            user.getId(), user.getEmail(), role, user.getFirstName(), user.getLastName());
    return new Cookie("jwt", token);
  }
}
