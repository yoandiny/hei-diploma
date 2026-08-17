package mg.yoan.diploma;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.http.HttpMethod.GET;

import java.util.List;
import mg.yoan.diploma.domain.Role;
import mg.yoan.diploma.domain.Teacher;
import mg.yoan.diploma.endpoint.rest.security.JwtService;
import mg.yoan.diploma.repository.model.JUser;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

class AuthPermissionIT extends DiplomaIT {

  @Autowired private TestRestTemplate restTemplate;
  @Autowired private JwtService jwtService;

  @Test
  void anonymous_is_sent_to_login() {
    assertTrue(sentTo(get("/admin/dashboard.html", null), "/login", "id=\"email\""));
    assertTrue(sentTo(get("/teacher/dashboard.html", null), "/login", "id=\"email\""));
  }

  @Test
  void teacher_cannot_open_admin_pages() {
    Teacher teacher = newTeacher();
    assertTrue(
        sentTo(
            get("/admin/dashboard.html", jwt(teacher.getId().toString(), Role.TEACHER)),
            "/forbidden",
            "pas le droit"));
  }

  @Test
  void admin_cannot_open_teacher_pages() {
    JUser admin = newAdmin();
    assertTrue(
        sentTo(
            get("/teacher/dashboard.html", jwt(admin.getId(), Role.ADMIN)),
            "/forbidden",
            "pas le droit"));
  }

  @Test
  void teacher_reaches_teacher_home() {
    Teacher teacher = newTeacher();
    String body =
        get("/teacher/dashboard.html", jwt(teacher.getId().toString(), Role.TEACHER)).getBody();
    assertTrue(body != null && body.contains("Dashboard enseignant"));
  }

  @Test
  void admin_reaches_admin_home() {
    JUser admin = newAdmin();
    String token = jwt(admin.getId(), Role.ADMIN);
    String dashboard = get("/admin/dashboard.html", token).getBody();
    String students = get("/admin/students/list.html", token).getBody();
    assertTrue(dashboard != null && dashboard.contains("Dashboard admin"));
    assertTrue(students != null && students.contains("Étudiants"));
  }

  @Test
  void disabled_teacher_token_is_rejected() {
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
    assertTrue(
        sentTo(
            get("/teacher/dashboard.html", jwt(teacher.getId().toString(), Role.TEACHER)),
            "/login",
            "id=\"email\""));
  }

  private ResponseEntity<String> get(String path, String token) {
    HttpHeaders headers = new HttpHeaders();
    headers.setAccept(List.of(MediaType.TEXT_HTML));
    if (token != null) {
      headers.add(HttpHeaders.COOKIE, "jwt=" + token);
    }
    return restTemplate.exchange(path, GET, new HttpEntity<>(headers), String.class);
  }

  private String jwt(String userId, Role role) {
    JUser user = userRepository.findById(userId).orElseThrow();
    return jwtService.generateToken(
        user.getId(), user.getEmail(), role, user.getFirstName(), user.getLastName());
  }

  private static boolean sentTo(ResponseEntity<String> response, String path, String pageMarker) {
    var location = response.getHeaders().getLocation();
    if (location != null && location.toString().contains(path)) {
      return true;
    }
    String body = response.getBody();
    return body != null && body.contains(pageMarker);
  }
}
