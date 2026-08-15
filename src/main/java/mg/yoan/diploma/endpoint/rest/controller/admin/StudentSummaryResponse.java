package mg.yoan.diploma.endpoint.rest.controller.admin;

import mg.yoan.diploma.domain.Student;

public record StudentSummaryResponse(
    String id,
    String studentNumber,
    String firstName,
    String lastName,
    String email,
    String groupRef,
    String promotionLabel,
    Integer promotionStartYear) {

  public static StudentSummaryResponse from(Student student) {
    var user = student.getUser();
    var group = student.getCurrentGroup();
    var promotion = student.getPromotion();
    return new StudentSummaryResponse(
        student.getId().toString(),
        student.getStudentNumber(),
        user == null ? null : user.getFirstName(),
        user == null ? null : user.getLastName(),
        user == null ? null : user.getEmail(),
        group == null ? null : group.getRef(),
        promotion == null ? null : promotion.getLabel(),
        promotion == null ? null : promotion.getStartYear());
  }
}
