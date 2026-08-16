package mg.yoan.diploma.domain;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class Transcript {
  public static final BigDecimal PASSING_GRADE = new BigDecimal("10");

  private final Student student;
  private final List<Grade> grades;

  public BigDecimal weightedAverage() {
    return weightedAverageOf(grades);
  }

  public boolean isGraduated(Collection<UUID> requiredCourseIds) {
    if (requiredCourseIds == null || requiredCourseIds.isEmpty()) {
      return false;
    }
    BigDecimal average = weightedAverage();
    if (average == null || average.compareTo(PASSING_GRADE) < 0) {
      return false;
    }
    Map<UUID, List<Grade>> gradesByCourse =
        grades.stream()
            .filter(grade -> grade.getExam() != null && grade.getExam().getCourse() != null)
            .collect(Collectors.groupingBy(grade -> grade.getExam().getCourse().getId()));

    for (UUID courseId : requiredCourseIds) {
      List<Grade> courseGrades = gradesByCourse.getOrDefault(courseId, List.of());
      BigDecimal courseAverage = weightedAverageOf(courseGrades);
      if (courseAverage == null || courseAverage.compareTo(PASSING_GRADE) < 0) {
        return false;
      }
    }
    return true;
  }

  private static BigDecimal weightedAverageOf(List<Grade> values) {
    BigDecimal weightedSum = BigDecimal.ZERO;
    BigDecimal coefficientSum = BigDecimal.ZERO;

    for (Grade grade : values) {
      weightedSum = weightedSum.add(grade.getValue());
      coefficientSum = coefficientSum.add(grade.getExam().getCoefficient());
    }

    if (coefficientSum.compareTo(BigDecimal.ZERO) == 0) {
      return null;
    }
    return weightedSum.divide(coefficientSum, 2, RoundingMode.HALF_UP);
  }
}
