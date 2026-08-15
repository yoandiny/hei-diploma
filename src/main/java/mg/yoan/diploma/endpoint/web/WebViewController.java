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

  @GetMapping("/teacher/dashboard.html")
  @PreAuthorize("hasRole('TEACHER')")
  public String teacherDashboard(Model model) {
    return page(model, "teacher/dashboard", "Dashboard enseignant", "dashboard");
  }

  @GetMapping("/teacher/courses.html")
  @PreAuthorize("hasRole('TEACHER')")
  public String teacherCourses(Model model) {
    return page(model, "teacher/courses", "Mes cours", "cours");
  }

  @GetMapping("/teacher/grades-home.html")
  @PreAuthorize("hasRole('TEACHER')")
  public String teacherGradesHome(Model model) {
    return page(model, "teacher/grades-home", "Notes de mes cours", "notes");
  }

  @GetMapping("/teacher/grades-edit.html")
  @PreAuthorize("hasRole('TEACHER')")
  public String teacherGradesEdit(Model model) {
    return page(model, "teacher/grades-edit", "Saisie des notes", "notes");
  }

  @GetMapping("/teacher/grade-history.html")
  @PreAuthorize("hasRole('TEACHER')")
  public String teacherGradeHistory(Model model) {
    return page(model, "teacher/grade-history", "Historique de note", "notes");
  }

  @GetMapping("/admin/dashboard.html")
  @PreAuthorize("hasRole('ADMIN')")
  public String adminDashboard(Model model) {
    return page(model, "admin/dashboard", "Dashboard admin", "dashboard");
  }

  @GetMapping("/admin/students/edit-group.html")
  @PreAuthorize("hasRole('ADMIN')")
  public String adminEditGroup(Model model) {
    return page(model, "admin/students/edit-group", "Changer de groupe", "etudiants");
  }

  @GetMapping("/admin/grades/history.html")
  @PreAuthorize("hasRole('ADMIN')")
  public String adminGradeHistory(Model model) {
    return page(model, "admin/grades/history", "Historique des notes", "historique");
  }

  @GetMapping("/admin/promotion/results.html")
  @PreAuthorize("hasRole('ADMIN')")
  public String adminPromotion(Model model) {
    return page(model, "admin/promotion/results", "Résultats de promotion (3 ans)", "promotion");
  }

  private String page(Model model, String view, String heading, String nav) {
    model.addAttribute("pageHeading", heading);
    model.addAttribute("activeNav", nav);
    return view;
  }
}
