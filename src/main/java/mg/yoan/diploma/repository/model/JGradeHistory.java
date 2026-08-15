package mg.yoan.diploma.repository.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "grade_history")
@Getter
@Setter
public class JGradeHistory {

  @Id private String id;

  @ManyToOne(optional = false, fetch = FetchType.LAZY)
  @JoinColumn(name = "grade_id", nullable = false)
  private JGrade grade;

  @Column(precision = 5, scale = 2)
  private BigDecimal previousValue;

  @Column(nullable = false, precision = 5, scale = 2)
  private BigDecimal newValue;

  @Column(nullable = false)
  private String reason;

  @ManyToOne(optional = false, fetch = FetchType.LAZY)
  @JoinColumn(name = "changed_by", nullable = false)
  private JUser changedBy;

  @Column(nullable = false)
  private Instant changedAt;
}
