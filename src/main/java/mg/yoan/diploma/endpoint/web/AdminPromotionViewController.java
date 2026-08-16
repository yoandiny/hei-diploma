package mg.yoan.diploma.endpoint.web;

import lombok.AllArgsConstructor;
import mg.yoan.diploma.domain.Promotion;
import mg.yoan.diploma.service.DomainException;
import mg.yoan.diploma.service.GraduatesExcelExporter;
import mg.yoan.diploma.service.GraduationService;
import mg.yoan.diploma.service.PromotionService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@AllArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminPromotionViewController {

  private static final MediaType XLSX =
      MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");

  private final PromotionService promotionService;
  private final GraduationService graduationService;
  private final GraduatesExcelExporter graduatesExcelExporter;

  @GetMapping("/admin/promotion/results.html")
  public String graduates(
      @RequestParam(value = "promotionId", required = false) String promotionId, Model model) {
    model.addAttribute("pageHeading", "Diplômés");
    model.addAttribute("activeNav", "promotion");
    model.addAttribute("promotions", promotionService.listAll());
    if (promotionId == null || promotionId.isBlank()) {
      return "admin/promotion/results";
    }
    try {
      model.addAttribute("selectedPromotionId", promotionId);
      model.addAttribute("promotion", promotionService.getById(promotionId));
      model.addAttribute("graduates", graduationService.listGraduates(promotionId));
    } catch (DomainException exception) {
      model.addAttribute("error", exception.getMessage());
    }
    return "admin/promotion/results";
  }

  @GetMapping("/admin/promotion/graduates.xlsx")
  public ResponseEntity<byte[]> exportExcel(@RequestParam("promotionId") String promotionId) {
    try {
      Promotion promotion = promotionService.getById(promotionId);
      byte[] excel =
          graduatesExcelExporter.write(promotion, graduationService.listGraduates(promotionId));
      String filename = "diplomes-" + safeFilename(promotion.getLabel()) + ".xlsx";
      return ResponseEntity.ok()
          .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
          .contentType(XLSX)
          .body(excel);
    } catch (DomainException exception) {
      return ResponseEntity.notFound().build();
    }
  }

  private static String safeFilename(String label) {
    String sanitized = label == null ? "promotion" : label.replaceAll("[^a-zA-Z0-9._-]+", "-");
    return sanitized.isBlank() ? "promotion" : sanitized;
  }
}
