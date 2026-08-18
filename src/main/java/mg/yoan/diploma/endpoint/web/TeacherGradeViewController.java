package mg.yoan.diploma.endpoint.web;

import java.math.BigDecimal;
import java.util.List;
import lombok.AllArgsConstructor;
import mg.yoan.diploma.endpoint.rest.security.AuthenticatedUser;
import mg.yoan.diploma.service.DomainException;
import mg.yoan.diploma.service.TeacherGradeService;
import mg.yoan.diploma.service.TeacherGradeService.ExamOption;
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

  private final TeacherGradeService teacherGradeService;

  @GetMapping("/teacher/dashboard.html")
  public String dashboard(@AuthenticationPrincipal AuthenticatedUser user, Model model) {
    var home = teacherGradeService.loadHome(user.getUserId());
    model.addAttribute("pageHeading", "Dashboard enseignant");
    model.addAttribute("activeNav", "dashboard");
    model.addAttribute("courses", home.courses());
    model.addAttribute("draftExams", home.draftExams());
    model.addAttribute("courseCount", home.courses().size());
    model.addAttribute("groupCount", home.groupCount());
    model.addAttribute("studentCount", home.studentCount());
    model.addAttribute("examCount", home.exams().size());
    model.addAttribute("openExamCount", home.draftExams().size());
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
  public String gradesHome(
      @AuthenticationPrincipal AuthenticatedUser user,
      @RequestParam(value = "courseId", required = false) String courseId,
      Model model) {
    List<ExamOption> exams = teacherGradeService.listTeacherExams(user.getUserId());
    if (courseId != null && !courseId.isBlank()) {
      exams = exams.stream().filter(exam -> exam.courseId().equals(courseId)).toList();
    }
    model.addAttribute("pageHeading", "Saisie des notes");
    model.addAttribute("activeNav", "notes");
    model.addAttribute("draftExams", exams.stream().filter(exam -> !exam.isSubmitted()).toList());
    model.addAttribute("submittedExams", exams.stream().filter(ExamOption::isSubmitted).toList());
    model.addAttribute("courses", teacherGradeService.listAssignedCourses(user.getUserId()));
    model.addAttribute("courseId", courseId);
    return "teacher/grades-home";
  }

  @GetMapping("/teacher/grades-edit.html")
  public String gradesEdit(
      @AuthenticationPrincipal AuthenticatedUser user,
      @RequestParam("examId") String examId,
      @RequestParam(value = "groupId", required = false) String groupId,
      Model model,
      RedirectAttributes redirectAttributes) {
    model.addAttribute("activeNav", "notes");
    try {
      var sheet = teacherGradeService.getGradeSheet(user.getUserId(), examId, groupId);
      model.addAttribute("sheet", sheet);
      model.addAttribute("exam", sheet.exam());
      model.addAttribute("examId", examId);
      model.addAttribute("groupId", groupId);
      model.addAttribute("examGroups", sheet.examGroups());
      model.addAttribute("pageHeading", "Saisie — " + sheet.courseRef());
      return "teacher/grades-edit";
    } catch (DomainException exception) {
      redirectAttributes.addFlashAttribute("error", exception.getMessage());
      return "redirect:/teacher/grades-home.html";
    }
  }

  @PostMapping("/teacher/grades")
  public String saveGrade(
      @AuthenticationPrincipal AuthenticatedUser user,
      @RequestParam("examId") String examId,
      @RequestParam(value = "groupId", required = false) String groupId,
      @RequestParam("studentId") String studentId,
      @RequestParam("value") BigDecimal value,
      @RequestParam(value = "reason", required = false) String reason,
      RedirectAttributes redirectAttributes) {
    try {
      teacherGradeService.saveGrade(user.getUserId(), examId, studentId, value, reason);
      redirectAttributes.addFlashAttribute("success", "Note enregistrée.");
    } catch (DomainException exception) {
      redirectAttributes.addFlashAttribute("error", exception.getMessage());
    }
    return redirectToEdit(examId, groupId);
  }

  @GetMapping("/teacher/grade-history.html")
  public String gradeHistory(
      @AuthenticationPrincipal AuthenticatedUser user,
      @RequestParam("gradeId") String gradeId,
      @RequestParam("examId") String examId,
      @RequestParam(value = "groupId", required = false) String groupId,
      Model model,
      RedirectAttributes redirectAttributes) {
    model.addAttribute("activeNav", "notes");
    try {
      var history = teacherGradeService.getHistory(user.getUserId(), gradeId);
      model.addAttribute("pageHeading", "Historique de note");
      model.addAttribute("history", history);
      model.addAttribute("examId", examId);
      model.addAttribute("groupId", groupId);
      return "teacher/grade-history";
    } catch (DomainException exception) {
      redirectAttributes.addFlashAttribute("error", exception.getMessage());
      return redirectToEdit(examId, groupId);
    }
  }

  private static String redirectToEdit(String examId, String groupId) {
    String redirect = "redirect:/teacher/grades-edit.html?examId=" + examId;
    if (groupId != null && !groupId.isBlank()) {
      redirect += "&groupId=" + groupId;
    }
    return redirect;
  }
}
