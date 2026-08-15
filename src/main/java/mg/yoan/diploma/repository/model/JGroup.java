package mg.yoan.diploma.repository.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "group")
@Getter
@Setter
public class JGroup {

  @Id private String id;

  @Column(nullable = false)
  private String ref;

  @ManyToOne(optional = false, fetch = FetchType.LAZY)
  @JoinColumn(name = "promotion_id", nullable = false)
  private JPromotion promotion;
}
