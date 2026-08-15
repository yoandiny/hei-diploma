package mg.yoan.diploma.endpoint.rest.controller.student;

import java.math.BigDecimal;
import java.util.List;
import mg.yoan.diploma.domain.Student;
import mg.yoan.diploma.domain.Transcript;

public record TranscriptResponse(
    String studentId,
    String studentNumber,
    String firstName,
    String lastName,
    String email,
    String promotionLabel,
    Integer promotionStartYear,
    Integer promotionEndYear,
    String groupRef,
    List<TranscriptGradeResponse> grades,
    BigDecimal weightedAverage) {

  public static TranscriptResponse from(Transcript transcript) {
    Student student = transcript.getStudent();
    var user = student.getUser();
    var promotion = student.getPromotion();
    var group = student.getCurrentGroup();

    return new TranscriptResponse(
        student.getId().toString(),
        student.getStudentNumber(),
        user == null ? null : user.getFirstName(),
        user == null ? null : user.getLastName(),
        user == null ? null : user.getEmail(),
        promotion == null ? null : promotion.getLabel(),
        promotion == null ? null : promotion.getStartYear(),
        promotion == null ? null : promotion.getEndYear(),
        group == null ? null : group.getRef(),
        transcript.getGrades().stream().map(TranscriptGradeResponse::from).toList(),
        transcript.weightedAverage());
  }
}
