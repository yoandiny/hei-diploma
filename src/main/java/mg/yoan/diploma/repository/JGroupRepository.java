package mg.yoan.diploma.repository;

import java.util.List;
import mg.yoan.diploma.repository.model.JGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JGroupRepository extends JpaRepository<JGroup, String> {

  List<JGroup> findByPromotionId(String promotionId);
}
