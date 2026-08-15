package mg.yoan.diploma.endpoint.rest.security;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JwtCookie {

  static final String NAME = "jwt";

  private final JwtService jwtService;

  public void write(
      HttpServletRequest request, HttpServletResponse response, AuthenticatedUser user) {
    String token =
        jwtService.generateToken(
            user.getUserId(),
            user.getEmail(),
            user.getRole(),
            user.getFirstName(),
            user.getLastName());
    addCookie(request, response, token, jwtService.getExpirationSeconds());
  }

  public void clear(HttpServletRequest request, HttpServletResponse response) {
    addCookie(request, response, "", 0);
  }

  public Optional<String> read(HttpServletRequest request) {
    Cookie[] cookies = request.getCookies();
    if (cookies == null) {
      return Optional.empty();
    }
    for (Cookie cookie : cookies) {
      if (NAME.equals(cookie.getName())
          && cookie.getValue() != null
          && !cookie.getValue().isBlank()) {
        return Optional.of(cookie.getValue());
      }
    }
    return Optional.empty();
  }

  private void addCookie(
      HttpServletRequest request, HttpServletResponse response, String value, long maxAgeSeconds) {
    ResponseCookie cookie =
        ResponseCookie.from(NAME, value)
            .httpOnly(true)
            .secure(isSecure(request))
            .path("/")
            .maxAge(maxAgeSeconds)
            .sameSite("Lax")
            .build();
    response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
  }

  private boolean isSecure(HttpServletRequest request) {
    if (request.isSecure()) {
      return true;
    }
    String forwarded = request.getHeader("X-Forwarded-Proto");
    return forwarded != null && forwarded.toLowerCase().contains("https");
  }
}
