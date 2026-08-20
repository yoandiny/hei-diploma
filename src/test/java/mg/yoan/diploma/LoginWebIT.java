package mg.yoan.diploma;

import static org.junit.jupiter.api.Assertions.assertTrue;

import mg.yoan.diploma.domain.Teacher;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

class LoginWebIT extends WebDiplomaIT {

  @Test
  void form_login_sets_jwt_cookie() {
    Teacher teacher = newTeacher();

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
    headers.setAccept(java.util.List.of(MediaType.TEXT_HTML));

    ResponseEntity<String> response =
        restTemplate.exchange(
            "/login",
            HttpMethod.POST,
            new HttpEntity<>(
                form("email", teacher.getUser().getEmail(), "password", PASSWORD), headers),
            String.class);

    assertTrue(response.getHeaders().containsKey(HttpHeaders.SET_COOKIE));
    assertTrue(
        response.getHeaders().get(HttpHeaders.SET_COOKIE).stream()
            .anyMatch(cookie -> cookie.startsWith("jwt=")));
  }

  @Test
  void login_page_and_forbidden_page_are_public() {
    assertContains(htmlGet("/login", null).getBody(), "id=\"email\"");
    assertContains(htmlGet("/", null).getBody(), "id=\"email\"");
    assertContains(htmlGet("/forbidden", null).getBody(), "pas le droit");
  }
}
