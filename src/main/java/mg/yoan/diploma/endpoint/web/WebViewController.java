package mg.yoan.diploma.endpoint.web;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class WebViewController {

  @GetMapping("/student/dashboard.html")
  @PreAuthorize("hasRole('STUDENT')")
  public String studentDashboard(Model model) {
    return page(model, "student/dashboard", "Dashboard", "dashboard");
  }

  @GetMapping("/student/profile.html")
  @PreAuthorize("hasRole('STUDENT')")
  public String studentProfile(Model model) {
    return page(model, "student/profile", "Mon profil", "profil");
  }

  @GetMapping("/student/grades.html")
  @PreAuthorize("hasRole('STUDENT')")
  public String studentGrades(Model model) {
    return page(model, "student/grades", "Mes notes", "notes");
  }

  @GetMapping("/student/transcripts.html")
  @PreAuthorize("hasRole('STUDENT')")
  public String studentTranscripts(Model model) {
    return page(model, "student/transcripts", "Mes relevés", "releves");
  }

  private String page(Model model, String view, String heading, String nav) {
    model.addAttribute("pageHeading", heading);
    model.addAttribute("activeNav", nav);
    return view;
  }
}
