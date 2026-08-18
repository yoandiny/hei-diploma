package mg.yoan.diploma.endpoint.web;

import lombok.AllArgsConstructor;
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
public class AdminTeacherViewController {

  private final TeacherService teacherService;

  @GetMapping("/admin/teachers/list.html")
  public String list(Model model) {
    model.addAttribute("pageHeading", "Enseignants");
    model.addAttribute("activeNav", "enseignants");
    model.addAttribute("teachers", teacherService.listAll());
    return "admin/teachers/list";
  }

  @GetMapping("/admin/teachers/form.html")
  public String form(
      @RequestParam(value = "id", required = false) String id,
      Model model,
      RedirectAttributes redirectAttributes) {
    model.addAttribute("activeNav", "enseignants");
    if (id == null || id.isBlank()) {
      model.addAttribute("pageHeading", "Nouvel enseignant");
      model.addAttribute("teacher", null);
      return "admin/teachers/form";
    }
    try {
      model.addAttribute("pageHeading", "Éditer un enseignant");
      model.addAttribute("teacher", teacherService.getById(id));
      return "admin/teachers/form";
    } catch (DomainException exception) {
      redirectAttributes.addAttribute("error", exception.getMessage());
      return "redirect:/admin/teachers/list.html";
    }
  }

  @PostMapping("/admin/teachers/save")
  public String save(
      @RequestParam(value = "id", required = false) String id,
      @RequestParam("firstName") String firstName,
      @RequestParam("lastName") String lastName,
      @RequestParam("email") String email,
      @RequestParam("employeeNumber") String employeeNumber,
      @RequestParam(value = "password", required = false) String password,
      @RequestParam(value = "enabled", defaultValue = "false") boolean enabled,
      RedirectAttributes redirectAttributes) {
    try {
      if (id == null || id.isBlank()) {
        teacherService.create(firstName, lastName, email, employeeNumber, password, enabled);
      } else {
        teacherService.update(id, firstName, lastName, email, employeeNumber, password, enabled);
      }
      redirectAttributes.addAttribute("success", "Enseignant enregistré.");
      return "redirect:/admin/teachers/list.html";
    } catch (DomainException exception) {
      redirectAttributes.addAttribute("error", exception.getMessage());
      if (id == null || id.isBlank()) {
        return "redirect:/admin/teachers/form.html";
      }
      return "redirect:/admin/teachers/form.html?id=" + id;
    }
  }

  @PostMapping("/admin/teachers/{id}/delete")
  public String delete(@PathVariable String id, RedirectAttributes redirectAttributes) {
    try {
      teacherService.delete(id);
      redirectAttributes.addAttribute("success", "Enseignant supprimé.");
    } catch (DomainException exception) {
      redirectAttributes.addAttribute("error", exception.getMessage());
    }
    return "redirect:/admin/teachers/list.html";
  }
}
