package mg.yoan.diploma.endpoint.web;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import mg.yoan.diploma.service.AdminGradeService;
import mg.yoan.diploma.service.DomainException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@AllArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminGradeViewController {

  private final AdminGradeService adminGradeService;

  @GetMapping("/admin/grades/home.html")
  public String gradesHome(Model model) {
    model.addAttribute("pageHeading", "Gestion des notes");
    model.addAttribute("activeNav", "notes");
    model.addAttribute("courses", adminGradeService.listAllCourses());
    return "admin/grades/home";
  }

  @GetMapping("/admin/grades/select-exam.html")
  public String selectExam(
      @RequestParam("courseId") String courseId, Model model, RedirectAttributes redirectAttributes) {
    try {
      model.addAttribute("pageHeading", "Sélectionner un examen");
      model.addAttribute("activeNav", "notes");
      model.addAttribute("courseId", courseId);
      model.addAttribute("exams", adminGradeService.listExamsByCourse(courseId));
      return "admin/grades/select-exam";
    } catch (DomainException exception) {
      redirectAttributes.addFlashAttribute("error", exception.getMessage());
      return "redirect:/admin/grades/home.html";
    }
  }

  @GetMapping("/admin/grades/select-group.html")
  public String selectGroup(
      @RequestParam("examId") String examId,
      @RequestParam("courseId") String courseId,
      Model model,
      RedirectAttributes redirectAttributes) {
    try {
      model.addAttribute("pageHeading", "Sélectionner un groupe");
      model.addAttribute("activeNav", "notes");
      model.addAttribute("examId", examId);
      model.addAttribute("courseId", courseId);
      model.addAttribute("groups", adminGradeService.listGroupsByExam(examId));
      return "admin/grades/select-group";
    } catch (DomainException exception) {
      redirectAttributes.addFlashAttribute("error", exception.getMessage());
      return "redirect:/admin/grades/select-exam.html?courseId=" + courseId;
    }
  }

  @GetMapping("/admin/grades/edit.html")
  public String gradesEdit(
      @RequestParam("examId") String examId,
      @RequestParam("groupId") String groupId,
      @RequestParam("courseId") String courseId,
      Model model,
      RedirectAttributes redirectAttributes) {
    try {
      var sheet = adminGradeService.getGradeSheet(examId, groupId);
      model.addAttribute("pageHeading", "Saisie — " + sheet.courseRef());
      model.addAttribute("activeNav", "notes");
      model.addAttribute("sheet", sheet);
      model.addAttribute("exam", sheet.exam());
      model.addAttribute("examId", examId);
      model.addAttribute("groupId", groupId);
      model.addAttribute("courseId", courseId);
      return "admin/grades/edit";
    } catch (DomainException exception) {
      redirectAttributes.addFlashAttribute("error", exception.getMessage());
      return "redirect:/admin/grades/select-group.html?examId=" + examId + "&courseId=" + courseId;
    }
  }

  @PostMapping("/admin/grades/save")
  public String saveGrade(
      @RequestParam("examId") String examId,
      @RequestParam("groupId") String groupId,
      @RequestParam("courseId") String courseId,
      @RequestParam("studentId") String studentId,
      @RequestParam("value") BigDecimal value,
      @RequestParam(value = "reason", required = false) String reason,
      RedirectAttributes redirectAttributes) {
    try {
      adminGradeService.saveGrade(null, examId, studentId, value, reason);
      redirectAttributes.addFlashAttribute("success", "Note enregistrée.");
    } catch (DomainException exception) {
      redirectAttributes.addFlashAttribute("error", exception.getMessage());
    }
    return "redirect:/admin/grades/edit.html?examId=" + examId + "&groupId=" + groupId
        + "&courseId=" + courseId;
  }

  @GetMapping("/admin/grades/grade-history.html")
  public String gradeHistory(
      @RequestParam("gradeId") String gradeId,
      @RequestParam("examId") String examId,
      @RequestParam("groupId") String groupId,
      @RequestParam("courseId") String courseId,
      Model model,
      RedirectAttributes redirectAttributes) {
    try {
      var history = adminGradeService.getHistory(gradeId);
      model.addAttribute("pageHeading", "Historique de note");
      model.addAttribute("activeNav", "notes");
      model.addAttribute("history", history);
      model.addAttribute("examId", examId);
      model.addAttribute("groupId", groupId);
      model.addAttribute("courseId", courseId);
      return "admin/grades/grade-history";
    } catch (DomainException exception) {
      redirectAttributes.addFlashAttribute("error", exception.getMessage());
      return "redirect:/admin/grades/edit.html?examId=" + examId + "&groupId=" + groupId
          + "&courseId=" + courseId;
    }
  }
}
