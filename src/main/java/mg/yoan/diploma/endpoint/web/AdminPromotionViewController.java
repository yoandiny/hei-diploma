package mg.yoan.diploma.endpoint.web;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import lombok.AllArgsConstructor;
import mg.yoan.diploma.domain.Promotion;
import mg.yoan.diploma.service.DomainException;
import mg.yoan.diploma.service.GraduatesExcelExporter;
import mg.yoan.diploma.service.GraduationService;
import mg.yoan.diploma.service.GraduationService.GraduatedStudent;
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
      List<GraduatedStudent> graduates = graduationService.listGraduates(promotionId);
      model.addAttribute("graduates", graduates);
      model.addAttribute("graduateCount", graduates.size());
      model.addAttribute("promotionAverage", averageOf(graduates));
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

  private static BigDecimal averageOf(List<GraduatedStudent> graduates) {
    List<BigDecimal> averages =
        graduates.stream()
            .map(GraduatedStudent::weightedAverage)
            .filter(value -> value != null)
            .toList();
    if (averages.isEmpty()) {
      return null;
    }
    BigDecimal sum = averages.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
    return sum.divide(BigDecimal.valueOf(averages.size()), 2, RoundingMode.HALF_UP);
  }

  private static String safeFilename(String label) {
    String sanitized = label == null ? "promotion" : label.replaceAll("[^a-zA-Z0-9._-]+", "-");
    return sanitized.isBlank() ? "promotion" : sanitized;
  }
}
