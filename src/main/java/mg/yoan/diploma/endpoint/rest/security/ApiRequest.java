package mg.yoan.diploma.endpoint.rest.security;

import jakarta.servlet.http.HttpServletRequest;

final class ApiRequest {

  private ApiRequest() {}

  static boolean isApi(HttpServletRequest request) {
    String accept = request.getHeader("Accept");
    if (accept != null && accept.contains("application/json") && !accept.contains("text/html")) {
      return true;
    }
    String path = request.getServletPath();
    return path.startsWith("/students")
        || path.startsWith("/auth/")
        || path.equals("/admin/students")
        || path.equals("/ping")
        || path.startsWith("/health/");
  }
}
