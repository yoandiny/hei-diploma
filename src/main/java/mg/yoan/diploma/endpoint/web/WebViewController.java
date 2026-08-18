package mg.yoan.diploma.endpoint.web;

import lombok.AllArgsConstructor;
import mg.yoan.diploma.endpoint.rest.controller.student.TranscriptResponse;
import mg.yoan.diploma.endpoint.rest.security.AuthenticatedUser;
import mg.yoan.diploma.service.TranscriptService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@AllArgsConstructor
public class WebViewController {

  private final TranscriptService transcriptService;

  private void addTranscript(Model model, AuthenticatedUser user) {
    transcriptService
        .getByStudentId(user.getUserId())
        .map(TranscriptResponse::from)
        .ifPresent(transcript -> model.addAttribute("transcript", transcript));
  }

  @GetMapping("/student/dashboard.html")
  @PreAuthorize("hasRole('STUDENT')")
  public String studentDashboard(@AuthenticationPrincipal AuthenticatedUser user, Model model) {
    addTranscript(model, user);
    return page(model, "student/dashboard", "Dashboard", "dashboard");
  }

  @GetMapping("/student/profile.html")
  @PreAuthorize("hasRole('STUDENT')")
  public String studentProfile(@AuthenticationPrincipal AuthenticatedUser user, Model model) {
    addTranscript(model, user);
    return page(model, "student/profile", "Mon profil", "profil");
  }

  @GetMapping("/student/grades.html")
  @PreAuthorize("hasRole('STUDENT')")
  public String studentGrades(@AuthenticationPrincipal AuthenticatedUser user, Model model) {
    addTranscript(model, user);
    return page(model, "student/grades", "Mes notes", "notes");
  }

  @GetMapping("/student/transcripts.html")
  @PreAuthorize("hasRole('STUDENT')")
  public String studentTranscripts() {
    return "redirect:/student/grades.html";
  }

  private String page(Model model, String view, String heading, String nav) {
    model.addAttribute("pageHeading", heading);
    model.addAttribute("activeNav", nav);
    return view;
  }
}
