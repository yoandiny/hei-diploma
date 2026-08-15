package mg.yoan.diploma.endpoint.web;

import lombok.AllArgsConstructor;
import mg.yoan.diploma.service.StudentService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@AllArgsConstructor
public class AdminStudentViewController {

  private final StudentService studentService;

  @GetMapping("/admin/students/list.html")
  @PreAuthorize("hasRole('ADMIN')")
  public String list(Model model) {
    model.addAttribute("pageHeading", "Étudiants");
    model.addAttribute("activeNav", "etudiants");
    model.addAttribute("students", studentService.listAll());
    return "admin/students/list";
  }
}
