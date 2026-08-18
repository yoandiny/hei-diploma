package mg.yoan.diploma.repository;

import java.util.List;
import java.util.Optional;
import mg.yoan.diploma.repository.model.JGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface JGroupRepository extends JpaRepository<JGroup, String> {

  List<JGroup> findByPromotionId(String promotionId);

  boolean existsByPromotionId(String promotionId);

  boolean existsByRefIgnoreCaseAndPromotionId(String ref, String promotionId);

  boolean existsByRefIgnoreCaseAndPromotionIdAndIdNot(String ref, String promotionId, String id);

  @Query(
      """
      select distinct g from JGroup g
      join fetch g.promotion
      order by g.ref
      """)
  List<JGroup> findAllDetailed();

  @Query(
      """
      select distinct g from JGroup g
      join fetch g.promotion
      where g.promotion.id = :promotionId
      order by g.ref
      """)
  List<JGroup> findDetailedByPromotionId(@Param("promotionId") String promotionId);

  @Query(
      """
      select g from JGroup g
      join fetch g.promotion
      where g.id = :id
      """)
  Optional<JGroup> findDetailedById(@Param("id") String id);
}
