package mg.yoan.diploma.endpoint.rest.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

@Component
public class LoginFailureHandler implements AuthenticationFailureHandler {

  @Override
  public void onAuthenticationFailure(
      HttpServletRequest request, HttpServletResponse response, AuthenticationException exception)
      throws IOException {
    String error = "credentials";
    if (exception instanceof DisabledException) {
      error = "disabled";
    } else if (exception instanceof InternalAuthenticationServiceException) {
      error = "server";
    }
    response.sendRedirect(request.getContextPath() + "/login?error=" + error);
  }
}
