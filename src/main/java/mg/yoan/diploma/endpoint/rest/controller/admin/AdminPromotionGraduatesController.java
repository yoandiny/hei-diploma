package mg.yoan.diploma.endpoint.rest.controller.admin;

import java.math.BigDecimal;
import java.util.List;
import lombok.AllArgsConstructor;
import mg.yoan.diploma.domain.Student;
import mg.yoan.diploma.service.DomainException;
import mg.yoan.diploma.service.GraduationService;
import mg.yoan.diploma.service.GraduationService.GraduatedStudent;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/admin/promotions")
@AllArgsConstructor
public class AdminPromotionGraduatesController {

  private final GraduationService graduationService;

  @GetMapping("/{promotionId}/graduates")
  @PreAuthorize("hasRole('ADMIN')")
  public List<GraduatedStudentResponse> listGraduates(@PathVariable String promotionId) {
    try {
      return graduationService.listGraduates(promotionId).stream()
          .map(GraduatedStudentResponse::from)
          .toList();
    } catch (DomainException exception) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, exception.getMessage());
    }
  }

  public record GraduatedStudentResponse(
      String id,
      String studentNumber,
      String firstName,
      String lastName,
      String email,
      String groupRef,
      String promotionLabel,
      Integer promotionStartYear,
      Integer promotionEndYear,
      BigDecimal weightedAverage) {

    static GraduatedStudentResponse from(GraduatedStudent graduate) {
      Student student = graduate.student();
      var user = student.getUser();
      var group = student.getCurrentGroup();
      var promotion = student.getPromotion();
      return new GraduatedStudentResponse(
          student.getId().toString(),
          student.getStudentNumber(),
          user == null ? null : user.getFirstName(),
          user == null ? null : user.getLastName(),
          user == null ? null : user.getEmail(),
          group == null ? null : group.getRef(),
          promotion == null ? null : promotion.getLabel(),
          promotion == null ? null : promotion.getStartYear(),
          promotion == null ? null : promotion.getEndYear(),
          graduate.weightedAverage());
    }
  }
}
