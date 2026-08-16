package mg.yoan.diploma.endpoint.web;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;
import java.util.List;
import lombok.AllArgsConstructor;
import mg.yoan.diploma.endpoint.rest.security.AuthenticatedUser;
import mg.yoan.diploma.service.DomainException;
import mg.yoan.diploma.service.TeacherGradeService;
import mg.yoan.diploma.service.TeacherGradeService.AssignedCourse;
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
public class TeacherExamViewController {

  private static final ZoneId ZONE = ZoneId.of("Africa/Nairobi");

  private final TeacherGradeService teacherGradeService;

  @GetMapping("/teacher/exams.html")
  public String exams(
      @AuthenticationPrincipal AuthenticatedUser user,
      @RequestParam(value = "courseId", required = false) String courseId,
      Model model) {
    List<ExamOption> exams = teacherGradeService.listTeacherExams(user.getUserId());
    if (courseId != null && !courseId.isBlank()) {
      exams = exams.stream().filter(exam -> exam.courseId().equals(courseId)).toList();
    }
    model.addAttribute("pageHeading", "Mes examens");
    model.addAttribute("activeNav", "examens");
    model.addAttribute("exams", exams);
    model.addAttribute("courses", teacherGradeService.listAssignedCourses(user.getUserId()));
    model.addAttribute("courseId", courseId);
    return "teacher/exams";
  }

  @GetMapping("/teacher/exams-new.html")
  public String examsNew(
      @AuthenticationPrincipal AuthenticatedUser user,
      @RequestParam(value = "courseId", required = false) String courseId,
      Model model,
      RedirectAttributes redirectAttributes) {
    List<AssignedCourse> courses = teacherGradeService.listAssignedCourses(user.getUserId());
    model.addAttribute("pageHeading", "Nouvel examen");
    model.addAttribute("activeNav", "examens");
    model.addAttribute("courses", courses);
    model.addAttribute("courseId", courseId);
    if (courseId == null || courseId.isBlank()) {
      return "teacher/exams-new";
    }
    try {
      var course =
          courses.stream()
              .filter(item -> item.courseId().equals(courseId))
              .findFirst()
              .orElseThrow(() -> new DomainException("Vous n'êtes pas responsable de ce cours."));
      model.addAttribute("course", course);
      model.addAttribute("assignedGroups", course.groups());
      return "teacher/exams-new";
    } catch (DomainException exception) {
      redirectAttributes.addFlashAttribute("error", exception.getMessage());
      return "redirect:/teacher/exams-new.html";
    }
  }

  @GetMapping("/teacher/exams-detail.html")
  public String examDetail(
      @AuthenticationPrincipal AuthenticatedUser user,
      @RequestParam("examId") String examId,
      Model model,
      RedirectAttributes redirectAttributes) {
    model.addAttribute("activeNav", "examens");
    try {
      ExamOption exam = teacherGradeService.getExam(user.getUserId(), examId);
      model.addAttribute("exam", exam);
      model.addAttribute("pageHeading", "Examen — " + exam.courseRef());
      return "teacher/exams-detail";
    } catch (DomainException exception) {
      redirectAttributes.addFlashAttribute("error", exception.getMessage());
      return "redirect:/teacher/exams.html";
    }
  }

  @PostMapping("/teacher/exams")
  public String createExam(
      @AuthenticationPrincipal AuthenticatedUser user,
      @RequestParam("courseId") String courseId,
      @RequestParam("dateExam") String dateExam,
      @RequestParam("coefficient") BigDecimal coefficient,
      @RequestParam(value = "groupIds", required = false) List<String> groupIds,
      RedirectAttributes redirectAttributes) {
    try {
      ExamOption exam =
          teacherGradeService.createExam(
              user.getUserId(), courseId, parseDateTime(dateExam), coefficient, groupIds);
      redirectAttributes.addFlashAttribute("success", "Examen créé.");
      return "redirect:/teacher/exams-detail.html?examId=" + exam.examId();
    } catch (DomainException exception) {
      redirectAttributes.addFlashAttribute("error", exception.getMessage());
      return "redirect:/teacher/exams-new.html?courseId=" + courseId;
    }
  }

  @PostMapping("/teacher/exams/submit")
  public String submitExam(
      @AuthenticationPrincipal AuthenticatedUser user,
      @RequestParam("examId") String examId,
      RedirectAttributes redirectAttributes) {
    try {
      teacherGradeService.submitExam(user.getUserId(), examId);
      redirectAttributes.addFlashAttribute("success", "Examen soumis.");
    } catch (DomainException exception) {
      redirectAttributes.addFlashAttribute("error", exception.getMessage());
    }
    return "redirect:/teacher/exams-detail.html?examId=" + examId;
  }

  private static Instant parseDateTime(String value) {
    try {
      return LocalDateTime.parse(value).atZone(ZONE).toInstant();
    } catch (DateTimeParseException exception) {
      throw new DomainException("La date de l'examen est invalide.");
    }
  }
}
