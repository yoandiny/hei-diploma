package mg.yoan.diploma.endpoint.rest.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

@Component
public class WebAwareAccessDeniedHandler implements AccessDeniedHandler {

  @Override
  public void handle(
      HttpServletRequest request,
      HttpServletResponse response,
      AccessDeniedException accessDeniedException)
      throws IOException {
    if (ApiRequest.isApi(request)) {
      response.sendError(HttpServletResponse.SC_FORBIDDEN);
      return;
    }
    response.sendRedirect(request.getContextPath() + "/forbidden");
  }
}
