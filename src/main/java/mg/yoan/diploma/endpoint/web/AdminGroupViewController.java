package mg.yoan.diploma.endpoint.web;

import lombok.AllArgsConstructor;
import mg.yoan.diploma.service.DomainException;
import mg.yoan.diploma.service.GroupService;
import mg.yoan.diploma.service.PromotionService;
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
public class AdminGroupViewController {

  private final GroupService groupService;
  private final PromotionService promotionService;

  @GetMapping("/admin/groups/list.html")
  public String list(Model model) {
    model.addAttribute("pageHeading", "Groupes");
    model.addAttribute("activeNav", "groupes");
    model.addAttribute("groups", groupService.listAll());
    model.addAttribute("promotions", promotionService.listAll());
    return "admin/groups/list";
  }

  @GetMapping("/admin/groups/form.html")
  public String form(
      @RequestParam(value = "id", required = false) String id,
      Model model,
      RedirectAttributes redirectAttributes) {
    model.addAttribute("activeNav", "groupes");
    model.addAttribute("promotions", promotionService.listAll());
    if (id == null || id.isBlank()) {
      model.addAttribute("pageHeading", "Nouveau groupe");
      model.addAttribute("group", null);
      return "admin/groups/form";
    }
    try {
      model.addAttribute("pageHeading", "Éditer un groupe");
      model.addAttribute("group", groupService.getById(id));
      return "admin/groups/form";
    } catch (DomainException exception) {
      redirectAttributes.addAttribute("error", exception.getMessage());
      return "redirect:/admin/groups/list.html";
    }
  }

  @PostMapping("/admin/groups/save")
  public String save(
      @RequestParam(value = "id", required = false) String id,
      @RequestParam("ref") String ref,
      @RequestParam("promotionId") String promotionId,
      RedirectAttributes redirectAttributes) {
    try {
      groupService.save(id, ref, promotionId);
      redirectAttributes.addAttribute("success", "Groupe enregistré.");
      return "redirect:/admin/groups/list.html";
    } catch (DomainException exception) {
      redirectAttributes.addAttribute("error", exception.getMessage());
      if (id == null || id.isBlank()) {
        return "redirect:/admin/groups/form.html";
      }
      return "redirect:/admin/groups/form.html?id=" + id;
    }
  }

  @PostMapping("/admin/groups/{id}/delete")
  public String delete(@PathVariable String id, RedirectAttributes redirectAttributes) {
    try {
      groupService.delete(id);
      redirectAttributes.addAttribute("success", "Groupe supprimé.");
    } catch (DomainException exception) {
      redirectAttributes.addAttribute("error", exception.getMessage());
    }
    return "redirect:/admin/groups/list.html";
  }

  @GetMapping("/admin/promotions/form.html")
  public String promotionForm(
      @RequestParam(value = "id", required = false) String id,
      Model model,
      RedirectAttributes redirectAttributes) {
    model.addAttribute("activeNav", "groupes");
    if (id == null || id.isBlank()) {
      model.addAttribute("pageHeading", "Nouvelle promotion");
      return "admin/promotions/form";
    }
    try {
      model.addAttribute("pageHeading", "Éditer une promotion");
      model.addAttribute("promotion", promotionService.getById(id));
      return "admin/promotions/form";
    } catch (DomainException exception) {
      redirectAttributes.addAttribute("error", exception.getMessage());
      return "redirect:/admin/groups/list.html";
    }
  }

  @PostMapping("/admin/promotions/save")
  public String savePromotion(
      @RequestParam(value = "id", required = false) String id,
      @RequestParam("label") String label,
      @RequestParam("startYear") Integer startYear,
      @RequestParam("endYear") Integer endYear,
      RedirectAttributes redirectAttributes) {
    try {
      promotionService.save(id, label, startYear, endYear);
      redirectAttributes.addAttribute("success", "Promotion enregistrée.");
      return "redirect:/admin/groups/list.html";
    } catch (DomainException exception) {
      redirectAttributes.addAttribute("error", exception.getMessage());
      if (id == null || id.isBlank()) {
        return "redirect:/admin/promotions/form.html";
      }
      return "redirect:/admin/promotions/form.html?id=" + id;
    }
  }

  @PostMapping("/admin/promotions/{id}/delete")
  public String deletePromotion(@PathVariable String id, RedirectAttributes redirectAttributes) {
    try {
      promotionService.delete(id);
      redirectAttributes.addAttribute("success", "Promotion supprimée.");
    } catch (DomainException exception) {
      redirectAttributes.addAttribute("error", exception.getMessage());
    }
    return "redirect:/admin/groups/list.html";
  }
}
