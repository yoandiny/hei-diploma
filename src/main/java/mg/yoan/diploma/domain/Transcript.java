package mg.yoan.diploma.domain;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class Transcript {
  private final Student student;
  private final List<Grade> grades;

  public BigDecimal weightedAverage() {
    BigDecimal weightedSum = BigDecimal.ZERO;
    BigDecimal coefficientSum = BigDecimal.ZERO;

    for (Grade grade : grades) {
      weightedSum = weightedSum.add(grade.getValue());
      coefficientSum = coefficientSum.add(grade.getExam().getCoefficient());
    }

    if (coefficientSum.compareTo(BigDecimal.ZERO) == 0) {
      return null;
    }
    return weightedSum.divide(coefficientSum, 2, RoundingMode.HALF_UP);
  }
}
