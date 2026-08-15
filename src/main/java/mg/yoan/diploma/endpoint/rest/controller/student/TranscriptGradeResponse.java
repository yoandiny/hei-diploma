package mg.yoan.diploma.endpoint.rest.controller.student;

import java.math.BigDecimal;
import java.time.Instant;
import mg.yoan.diploma.domain.Grade;

public record TranscriptGradeResponse(
    String courseRef,
    String courseTitle,
    int credits,
    Instant examDate,
    BigDecimal coefficient,
    BigDecimal value) {

  public static TranscriptGradeResponse from(Grade grade) {
    var course = grade.getExam().getCourse();
    return new TranscriptGradeResponse(
        course.getRef(),
        course.getTitle(),
        course.getCredits(),
        grade.getExam().getDateExam(),
        grade.getExam().getCoefficient(),
        grade.getValue());
  }
}
