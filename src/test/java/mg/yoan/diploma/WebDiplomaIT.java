package mg.yoan.diploma;

import java.util.List;
import mg.yoan.diploma.domain.Role;
import mg.yoan.diploma.domain.Student;
import mg.yoan.diploma.domain.Teacher;
import mg.yoan.diploma.endpoint.rest.security.JwtService;
import mg.yoan.diploma.repository.model.JUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

public abstract class WebDiplomaIT extends DiplomaIT {

  @Autowired protected TestRestTemplate restTemplate;
  @Autowired protected JwtService jwtService;

  protected String jwt(JUser user, Role role) {
    return jwtService.generateToken(
        user.getId(), user.getEmail(), role, user.getFirstName(), user.getLastName());
  }

  protected String adminJwt() {
    return jwt(newAdmin(), Role.ADMIN);
  }

  protected String teacherJwt(Teacher teacher) {
    JUser user = userRepository.findById(teacher.getId().toString()).orElseThrow();
    return jwt(user, Role.TEACHER);
  }

  protected String studentJwt(Student student) {
    JUser user = userRepository.findById(student.getId().toString()).orElseThrow();
    return jwt(user, Role.STUDENT);
  }

  protected ResponseEntity<String> htmlGet(String path, String token) {
    HttpHeaders headers = new HttpHeaders();
    headers.setAccept(List.of(MediaType.TEXT_HTML));
    if (token != null) {
      headers.add(HttpHeaders.COOKIE, "jwt=" + token);
    }
    return restTemplate.exchange(path, HttpMethod.GET, new HttpEntity<>(headers), String.class);
  }

  protected ResponseEntity<String> htmlPost(String path, String token, MultiValueMap<String, String> form) {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
    headers.setAccept(List.of(MediaType.TEXT_HTML));
    if (token != null) {
      headers.add(HttpHeaders.COOKIE, "jwt=" + token);
    }
    return restTemplate.exchange(path, HttpMethod.POST, new HttpEntity<>(form, headers), String.class);
  }

  protected MultiValueMap<String, String> form(String... keyValues) {
    MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
    for (int index = 0; index < keyValues.length; index += 2) {
      form.add(keyValues[index], keyValues[index + 1]);
    }
    return form;
  }

  protected static void assertContains(String body, String marker) {
    if (body == null || !body.contains(marker)) {
      throw new AssertionError("Expected page to contain: " + marker);
    }
  }
}
