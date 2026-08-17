package mg.yoan.diploma.endpoint.web;

import lombok.AllArgsConstructor;
import mg.yoan.diploma.service.AdminDashboardService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@AllArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminDashboardViewController {

  private final AdminDashboardService adminDashboardService;

  @GetMapping("/admin/dashboard.html")
  public String dashboard(Model model) {
    var home = adminDashboardService.loadHome();
    model.addAttribute("pageHeading", "Dashboard admin");
    model.addAttribute("activeNav", "dashboard");
    model.addAttribute("courseCount", home.courseCount());
    model.addAttribute("studentCount", home.studentCount());
    model.addAttribute("teacherCount", home.teacherCount());
    model.addAttribute("gradeChangeCount", home.gradeChangeCount());
    model.addAttribute("recentChanges", home.recentChanges());
    return "admin/dashboard";
  }

  @GetMapping("/admin/grades/history.html")
  public String gradeHistory(Model model) {
    model.addAttribute("pageHeading", "Historique des notes");
    model.addAttribute("activeNav", "historique");
    model.addAttribute("changes", adminDashboardService.listGradeChanges());
    return "admin/grades/history";
  }
}
