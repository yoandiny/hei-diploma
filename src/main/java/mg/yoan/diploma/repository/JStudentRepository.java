package mg.yoan.diploma.repository;

import java.util.List;
import java.util.Optional;
import mg.yoan.diploma.repository.model.JStudent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JStudentRepository extends JpaRepository<JStudent, String> {

  Optional<JStudent> findByStudentNumber(String studentNumber);

  List<JStudent> findByPromotionId(String promotionId);

  List<JStudent> findByCurrentGroupId(String groupId);
}
