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
@Table(name = "exam")
@Getter
@Setter
public class JExam {

  @Id private String id;

  @ManyToOne(optional = false, fetch = FetchType.LAZY)
  @JoinColumn(name = "course_id", nullable = false)
  private JCourse course;

  @Column(nullable = false)
  private Instant dateExam;

  @Column(nullable = false, precision = 5, scale = 2)
  private BigDecimal coefficient;
}
