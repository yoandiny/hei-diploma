package mg.yoan.diploma.endpoint.web;

import mg.yoan.diploma.endpoint.rest.security.AuthenticatedUser;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice(basePackages = "mg.yoan.diploma.endpoint.web")
public class CurrentUserAdvice {

  @ModelAttribute("currentUser")
  public AuthenticatedUser currentUser(@AuthenticationPrincipal AuthenticatedUser user) {
    return user;
  }
}
