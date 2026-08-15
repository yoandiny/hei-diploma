package mg.yoan.diploma.endpoint.rest.security;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

final class HttpErrorResponse {

  private HttpErrorResponse() {}

  static void send(HttpServletResponse response, int status) throws IOException {
    response.setStatus(status);
    response.setContentType("application/json");
    response.getWriter().write("{}");
    response.flushBuffer();
  }
}
