package mg.yoan.diploma.endpoint.web;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import mg.yoan.diploma.domain.Course;
import mg.yoan.diploma.domain.CourseAssignment;
import mg.yoan.diploma.domain.Group;
import mg.yoan.diploma.domain.Teacher;
import mg.yoan.diploma.service.CourseAssignmentService;
import mg.yoan.diploma.service.CourseService;
import mg.yoan.diploma.service.DomainException;
import mg.yoan.diploma.service.TeacherService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@AllArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminCourseViewController {

  private final CourseService courseService;
  private final CourseAssignmentService assignmentService;
  private final TeacherService teacherService;

  @GetMapping("/admin/courses/list.html")
  public String list(Model model) {
    model.addAttribute("pageHeading", "Gestion des cours");
    model.addAttribute("activeNav", "cours");
    var assignmentsByCourse = assignmentService.listGroupedByCourseId();
    model.addAttribute(
        "courseRows",
        courseService.listAll().stream()
            .map(
                course ->
                    new CourseRow(
                        course,
                        assignmentsByCourse.getOrDefault(course.getId().toString(), List.of())))
            .toList());
    return "admin/courses/list";
  }

  @GetMapping("/admin/courses/form.html")
  public String form(
      @RequestParam(value = "id", required = false) String id,
      Model model,
      RedirectAttributes redirectAttributes) {
    model.addAttribute("activeNav", "cours");
    if (id == null || id.isBlank()) {
      model.addAttribute("pageHeading", "Nouveau cours");
      model.addAttribute("course", null);
      return "admin/courses/form";
    }
    try {
      model.addAttribute("pageHeading", "Éditer un cours");
      model.addAttribute("course", courseService.getById(id));
      return "admin/courses/form";
    } catch (DomainException exception) {
      redirectAttributes.addAttribute("error", exception.getMessage());
      return "redirect:/admin/courses/list.html";
    }
  }

  @PostMapping("/admin/courses/save")
  public String save(
      @RequestParam(value = "id", required = false) String id,
      @RequestParam("ref") String ref,
      @RequestParam("title") String title,
      @RequestParam("credits") Integer credits,
      RedirectAttributes redirectAttributes) {
    try {
      courseService.save(id, ref, title, credits);
      redirectAttributes.addAttribute("success", "Cours enregistré.");
      return "redirect:/admin/courses/list.html";
    } catch (DomainException exception) {
      redirectAttributes.addAttribute("error", exception.getMessage());
      if (id == null || id.isBlank()) {
        return "redirect:/admin/courses/form.html";
      }
      return "redirect:/admin/courses/form.html?id=" + id;
    }
  }

  @PostMapping("/admin/courses/{id}/delete")
  public String delete(@PathVariable String id, RedirectAttributes redirectAttributes) {
    try {
      courseService.delete(id);
      redirectAttributes.addAttribute("success", "Cours supprimé.");
    } catch (DomainException exception) {
      redirectAttributes.addAttribute("error", exception.getMessage());
    }
    return "redirect:/admin/courses/list.html";
  }

  @GetMapping("/admin/courses/assign-groups.html")
  public String assignments(
      @RequestParam("courseId") String courseId,
      Model model,
      RedirectAttributes redirectAttributes) {
    try {
      var course = courseService.getById(courseId);
      model.addAttribute("pageHeading", "Affectations");
      model.addAttribute("activeNav", "cours");
      model.addAttribute("course", course);
      model.addAttribute("assignments", assignmentService.listByCourse(courseId));
      model.addAttribute("teachers", teacherService.listAll());
      model.addAttribute("groups", assignmentService.listGroups());
      return "admin/courses/assign-groups";
    } catch (DomainException exception) {
      redirectAttributes.addAttribute("error", exception.getMessage());
      return "redirect:/admin/courses/list.html";
    }
  }

  @GetMapping("/admin/courses/{courseId}/assignments")
  public String assignmentsGet(@PathVariable String courseId) {
    return "redirect:/admin/courses/assign-groups.html?courseId=" + courseId;
  }

  @PostMapping("/admin/courses/{courseId}/assignments")
  public String addAssignment(
      @PathVariable String courseId,
      @RequestParam("teacherId") String teacherId,
      @RequestParam(value = "groupIds", required = false) List<String> groupIds,
      RedirectAttributes redirectAttributes) {
    try {
      assignmentService.assign(courseId, teacherId, groupIds);
      redirectAttributes.addAttribute("success", "Affectation enregistrée.");
    } catch (DomainException exception) {
      redirectAttributes.addAttribute("error", exception.getMessage());
    }
    return "redirect:/admin/courses/assign-groups.html?courseId=" + courseId;
  }

  @PostMapping("/admin/courses/assignments/{id}/delete")
  public String deleteAssignment(
      @PathVariable String id,
      @RequestParam("courseId") String courseId,
      RedirectAttributes redirectAttributes) {
    try {
      assignmentService.delete(id);
      redirectAttributes.addAttribute("success", "Affectation retirée.");
    } catch (DomainException exception) {
      redirectAttributes.addAttribute("error", exception.getMessage());
    }
    return "redirect:/admin/courses/assign-groups.html?courseId=" + courseId;
  }

  public record CourseRow(Course course, List<CourseAssignment> assignments) {
    public List<Teacher> teachers() {
      Map<String, Teacher> unique = new LinkedHashMap<>();
      for (CourseAssignment assignment : assignments) {
        Teacher teacher = assignment.getTeacher();
        if (teacher == null || teacher.getId() == null) {
          continue;
        }
        unique.putIfAbsent(teacher.getId().toString(), teacher);
      }
      return List.copyOf(unique.values());
    }

    public List<Group> groups() {
      Map<String, Group> unique = new LinkedHashMap<>();
      for (CourseAssignment assignment : assignments) {
        Group group = assignment.getGroup();
        if (group == null || group.getId() == null) {
          continue;
        }
        unique.putIfAbsent(group.getId().toString(), group);
      }
      return List.copyOf(unique.values());
    }
  }
}
