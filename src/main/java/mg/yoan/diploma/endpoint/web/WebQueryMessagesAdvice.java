package mg.yoan.diploma.endpoint.web;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestParam;

@ControllerAdvice(basePackages = "mg.yoan.diploma.endpoint.web")
public class WebQueryMessagesAdvice {

  @ModelAttribute
  public void queryMessages(
      @RequestParam(value = "success", required = false) String success,
      @RequestParam(value = "error", required = false) String error,
      Model model) {
    if (success != null && !success.isBlank()) {
      model.addAttribute("success", success);
    }
    if (error != null && !error.isBlank()) {
      model.addAttribute("error", error);
    }
  }
}
