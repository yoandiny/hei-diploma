package mg.yoan.diploma.endpoint.web;

import mg.yoan.diploma.endpoint.rest.security.AuthenticatedUser;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LoginController {

  @GetMapping({"/", "/login"})
  public String login(@AuthenticationPrincipal AuthenticatedUser user) {
    if (user == null) {
      return "auth/login";
    }
    return switch (user.getRole()) {
      case STUDENT -> "redirect:/student/dashboard.html";
      case TEACHER -> "redirect:/teacher/dashboard.html";
      case ADMIN -> "redirect:/admin/dashboard.html";
    };
  }

  @GetMapping("/forbidden")
  public String forbidden() {
    return "error/403";
  }
}
