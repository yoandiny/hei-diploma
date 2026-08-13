package mg.yoan.diploma.repository.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class JStudent {

  @Id private String id;

  @OneToOne(optional = false, fetch = FetchType.LAZY)
  @MapsId
  @JoinColumn(name = "id")
  private JUser user;

  @Column(nullable = false, unique = true)
  private String studentNumber;

  @ManyToOne(optional = false, fetch = FetchType.LAZY)
  @JoinColumn(name = "promotion_id", nullable = false)
  private JPromotion promotion;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "current_group_id")
  private JGroup currentGroup;
}