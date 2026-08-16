package mg.yoan.diploma.endpoint.web;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;
import java.util.List;
import lombok.AllArgsConstructor;
import mg.yoan.diploma.domain.Exam;
import mg.yoan.diploma.endpoint.rest.security.AuthenticatedUser;
import mg.yoan.diploma.service.DomainException;
import mg.yoan.diploma.service.TeacherGradeService;
import mg.yoan.diploma.service.TeacherGradeService.AssignedCourse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@AllArgsConstructor
@PreAuthorize("hasRole('TEACHER')")
public class TeacherGradeViewController {

  private static final ZoneId ZONE = ZoneId.of("Africa/Nairobi");

  private final TeacherGradeService teacherGradeService;

  @GetMapping("/teacher/dashboard.html")
  public String dashboard(@AuthenticationPrincipal AuthenticatedUser user, Model model) {
    List<AssignedCourse> courses = teacherGradeService.listAssignedCourses(user.getUserId());
    model.addAttribute("pageHeading", "Dashboard enseignant");
    model.addAttribute("activeNav", "dashboard");
    model.addAttribute("courses", courses);
    model.addAttribute("courseCount", courses.size());
    model.addAttribute(
        "groupCount", courses.stream().mapToInt(course -> course.groups().size()).sum());
    model.addAttribute(
        "studentCount", courses.stream().mapToInt(AssignedCourse::studentCount).sum());
    return "teacher/dashboard";
  }

  @GetMapping("/teacher/courses.html")
  public String courses(@AuthenticationPrincipal AuthenticatedUser user, Model model) {
    model.addAttribute("pageHeading", "Mes cours");
    model.addAttribute("activeNav", "cours");
    model.addAttribute("courses", teacherGradeService.listAssignedCourses(user.getUserId()));
    return "teacher/courses";
  }

  @GetMapping("/teacher/grades-home.html")
  public String gradesHome(@AuthenticationPrincipal AuthenticatedUser user, Model model) {
    model.addAttribute("pageHeading", "Notes de mes cours");
    model.addAttribute("activeNav", "notes");
    model.addAttribute("courses", teacherGradeService.listAssignedCourses(user.getUserId()));
    return "teacher/grades-home";
  }

  @GetMapping("/teacher/grades-edit.html")
  public String gradesEdit(
      @AuthenticationPrincipal AuthenticatedUser user,
      @RequestParam("courseId") String courseId,
      @RequestParam("groupId") String groupId,
      @RequestParam(value = "examId", required = false) String examId,
      Model model,
      RedirectAttributes redirectAttributes) {
    model.addAttribute("activeNav", "notes");
    try {
      List<Exam> exams = teacherGradeService.listExams(user.getUserId(), courseId);
      model.addAttribute("exams", exams);
      model.addAttribute("courseId", courseId);
      model.addAttribute("groupId", groupId);
      if (exams.isEmpty()) {
        model.addAttribute("pageHeading", "Saisie des notes");
        return "teacher/grades-edit";
      }
      String selectedExamId =
          examId == null || examId.isBlank() ? exams.get(0).getId().toString() : examId;
      var sheet =
          teacherGradeService.getGradeSheet(user.getUserId(), courseId, groupId, selectedExamId);
      model.addAttribute("sheet", sheet);
      model.addAttribute("selectedExamId", selectedExamId);
      model.addAttribute(
          "pageHeading",
          "Saisie des notes — " + sheet.exam().getCourse().getRef() + " / " + sheet.groupRef());
      return "teacher/grades-edit";
    } catch (DomainException exception) {
      redirectAttributes.addFlashAttribute("error", exception.getMessage());
      return "redirect:/teacher/grades-home.html";
    }
  }

  @PostMapping("/teacher/exams")
  public String createExam(
      @AuthenticationPrincipal AuthenticatedUser user,
      @RequestParam("courseId") String courseId,
      @RequestParam("groupId") String groupId,
      @RequestParam("dateExam") String dateExam,
      @RequestParam("coefficient") BigDecimal coefficient,
      RedirectAttributes redirectAttributes) {
    try {
      Exam exam =
          teacherGradeService.createExam(
              user.getUserId(), courseId, parseDateTime(dateExam), coefficient);
      redirectAttributes.addFlashAttribute("success", "Examen créé.");
      return "redirect:/teacher/grades-edit.html?courseId="
          + courseId
          + "&groupId="
          + groupId
          + "&examId="
          + exam.getId();
    } catch (DomainException exception) {
      redirectAttributes.addFlashAttribute("error", exception.getMessage());
      return "redirect:/teacher/grades-edit.html?courseId=" + courseId + "&groupId=" + groupId;
    }
  }

  @PostMapping("/teacher/grades")
  public String saveGrade(
      @AuthenticationPrincipal AuthenticatedUser user,
      @RequestParam("courseId") String courseId,
      @RequestParam("groupId") String groupId,
      @RequestParam("examId") String examId,
      @RequestParam("studentId") String studentId,
      @RequestParam("value") BigDecimal value,
      @RequestParam("reason") String reason,
      RedirectAttributes redirectAttributes) {
    try {
      teacherGradeService.saveGrade(
          user.getUserId(), courseId, groupId, examId, studentId, value, reason);
      redirectAttributes.addFlashAttribute("success", "Note enregistrée.");
    } catch (DomainException exception) {
      redirectAttributes.addFlashAttribute("error", exception.getMessage());
    }
    return "redirect:/teacher/grades-edit.html?courseId="
        + courseId
        + "&groupId="
        + groupId
        + "&examId="
        + examId;
  }

  @GetMapping("/teacher/grade-history.html")
  public String gradeHistory(
      @AuthenticationPrincipal AuthenticatedUser user,
      @RequestParam("gradeId") String gradeId,
      @RequestParam("courseId") String courseId,
      @RequestParam("groupId") String groupId,
      @RequestParam("examId") String examId,
      Model model,
      RedirectAttributes redirectAttributes) {
    model.addAttribute("activeNav", "notes");
    try {
      var history = teacherGradeService.getHistory(user.getUserId(), gradeId);
      model.addAttribute("pageHeading", "Historique de note");
      model.addAttribute("history", history);
      model.addAttribute("courseId", courseId);
      model.addAttribute("groupId", groupId);
      model.addAttribute("examId", examId);
      return "teacher/grade-history";
    } catch (DomainException exception) {
      redirectAttributes.addFlashAttribute("error", exception.getMessage());
      return "redirect:/teacher/grades-edit.html?courseId="
          + courseId
          + "&groupId="
          + groupId
          + "&examId="
          + examId;
    }
  }

  private static java.time.Instant parseDateTime(String value) {
    try {
      return LocalDateTime.parse(value).atZone(ZONE).toInstant();
    } catch (DateTimeParseException exception) {
      throw new DomainException("La date de l'examen est invalide.");
    }
  }
}
