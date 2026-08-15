package mg.yoan.diploma.repository.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "promotion")
@Getter
@Setter
public class JPromotion {

  @Id private String id;

  @Column(nullable = false)
  private String label;

  @Column(nullable = false)
  private int startYear;

  @Column(nullable = false)
  private int endYear;
}
