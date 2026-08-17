package mg.yoan.diploma.endpoint.web;

import lombok.AllArgsConstructor;
import mg.yoan.diploma.service.DomainException;
import mg.yoan.diploma.service.GroupService;
import mg.yoan.diploma.service.PromotionService;
import mg.yoan.diploma.service.StudentService;
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
public class AdminStudentViewController {

  private final StudentService studentService;
  private final PromotionService promotionService;
  private final GroupService groupService;

  @GetMapping("/admin/students/list.html")
  public String list(Model model) {
    model.addAttribute("pageHeading", "Étudiants");
    model.addAttribute("activeNav", "etudiants");
    model.addAttribute("students", studentService.listAll());
    return "admin/students/list";
  }

  @GetMapping("/admin/students/form.html")
  public String form(Model model) {
    model.addAttribute("pageHeading", "Nouvel étudiant");
    model.addAttribute("activeNav", "etudiants");
    model.addAttribute("promotions", promotionService.listAll());
    model.addAttribute("groups", groupService.listAll());
    return "admin/students/form";
  }

  @GetMapping("/admin/students/profile.html")
  public String profile(
      @RequestParam("id") String id, Model model, RedirectAttributes redirectAttributes) {
    model.addAttribute("activeNav", "etudiants");
    try {
      var student = studentService.getById(id);
      model.addAttribute("pageHeading", student.getUser().getFullName());
      model.addAttribute("student", student);
      return "admin/students/profile";
    } catch (DomainException exception) {
      redirectAttributes.addFlashAttribute("error", exception.getMessage());
      return "redirect:/admin/students/list.html";
    }
  }

  @PostMapping("/admin/students/save")
  public String save(
      @RequestParam("firstName") String firstName,
      @RequestParam("lastName") String lastName,
      @RequestParam("email") String email,
      @RequestParam("studentNumber") String studentNumber,
      @RequestParam("promotionId") String promotionId,
      @RequestParam(value = "groupId", required = false) String groupId,
      @RequestParam("password") String password,
      @RequestParam(value = "enabled", defaultValue = "false") boolean enabled,
      RedirectAttributes redirectAttributes) {
    try {
      studentService.create(
          firstName, lastName, email, studentNumber, promotionId, groupId, password, enabled);
      redirectAttributes.addFlashAttribute("success", "Étudiant enregistré.");
      return "redirect:/admin/students/list.html";
    } catch (DomainException exception) {
      redirectAttributes.addFlashAttribute("error", exception.getMessage());
      return "redirect:/admin/students/form.html";
    }
  }

  @PostMapping("/admin/students/{id}/suspend")
  public String suspend(
      @PathVariable String id,
      @RequestParam(value = "redirect", required = false) String redirect,
      RedirectAttributes redirectAttributes) {
    try {
      studentService.suspend(id);
      redirectAttributes.addFlashAttribute("success", "Étudiant suspendu.");
    } catch (DomainException exception) {
      redirectAttributes.addFlashAttribute("error", exception.getMessage());
    }
    return studentRedirect(id, redirect);
  }

  @PostMapping("/admin/students/{id}/unsuspend")
  public String unsuspend(
      @PathVariable String id,
      @RequestParam(value = "redirect", required = false) String redirect,
      RedirectAttributes redirectAttributes) {
    try {
      studentService.unsuspend(id);
      redirectAttributes.addFlashAttribute("success", "Compte réactivé.");
    } catch (DomainException exception) {
      redirectAttributes.addFlashAttribute("error", exception.getMessage());
    }
    return studentRedirect(id, redirect);
  }

  @GetMapping("/admin/students/edit-group.html")
  public String editGroup(
      @RequestParam("id") String id, Model model, RedirectAttributes redirectAttributes) {
    model.addAttribute("activeNav", "etudiants");
    try {
      var student = studentService.getById(id);
      model.addAttribute("pageHeading", "Changer de groupe");
      model.addAttribute("student", student);
      model.addAttribute(
          "groups", groupService.listByPromotion(student.getPromotion().getId().toString()));
      return "admin/students/edit-group";
    } catch (DomainException exception) {
      redirectAttributes.addFlashAttribute("error", exception.getMessage());
      return "redirect:/admin/students/list.html";
    }
  }

  @PostMapping("/admin/students/{id}/group")
  public String changeGroup(
      @PathVariable String id,
      @RequestParam("groupId") String groupId,
      RedirectAttributes redirectAttributes) {
    try {
      studentService.changeGroup(id, groupId);
      redirectAttributes.addFlashAttribute("success", "Groupe mis à jour.");
    } catch (DomainException exception) {
      redirectAttributes.addFlashAttribute("error", exception.getMessage());
      return "redirect:/admin/students/edit-group.html?id=" + id;
    }
    return "redirect:/admin/students/list.html";
  }

  @PostMapping("/admin/students/{id}/delete")
  public String delete(@PathVariable String id, RedirectAttributes redirectAttributes) {
    try {
      studentService.softDelete(id);
      redirectAttributes.addFlashAttribute("success", "Étudiant supprimé.");
    } catch (DomainException exception) {
      redirectAttributes.addFlashAttribute("error", exception.getMessage());
    }
    return "redirect:/admin/students/list.html";
  }

  private static String studentRedirect(String id, String redirect) {
    if ("profile".equals(redirect)) {
      return "redirect:/admin/students/profile.html?id=" + id;
    }
    return "redirect:/admin/students/list.html";
  }
}
