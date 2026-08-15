package mg.yoan.diploma.repository;

import java.util.List;
import mg.yoan.diploma.repository.model.JPromotion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JPromotionRepository extends JpaRepository<JPromotion, String> {

  List<JPromotion> findAllByOrderByStartYearDescLabelAsc();
}
