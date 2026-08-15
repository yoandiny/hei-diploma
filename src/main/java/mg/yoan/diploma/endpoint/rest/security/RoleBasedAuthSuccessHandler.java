package mg.yoan.diploma.endpoint.rest.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

@Component
public class RoleBasedAuthSuccessHandler implements AuthenticationSuccessHandler {

  @Override
  public void onAuthenticationSuccess(
      HttpServletRequest request, HttpServletResponse response, Authentication authentication)
      throws IOException {
    response.sendRedirect(request.getContextPath() + homeFor(authentication));
  }

  private String homeFor(Authentication authentication) {
    if (authentication.getPrincipal() instanceof AuthenticatedUser user) {
      return switch (user.getRole()) {
        case STUDENT -> "/student/dashboard.html";
        case TEACHER -> "/teacher/dashboard.html";
        case ADMIN -> "/admin/dashboard.html";
      };
    }
    return "/";
  }
}
