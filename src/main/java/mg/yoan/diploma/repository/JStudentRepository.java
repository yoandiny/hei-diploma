package mg.yoan.diploma.repository;

import java.util.List;
import java.util.Optional;
import mg.yoan.diploma.repository.model.JStudent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface JStudentRepository extends JpaRepository<JStudent, String> {

  Optional<JStudent> findByStudentNumber(String studentNumber);

  List<JStudent> findByPromotionId(String promotionId);

  @Query(
      """
      select distinct s from JStudent s
      join fetch s.user
      join fetch s.promotion
      left join fetch s.currentGroup g
      left join fetch g.promotion
      where s.promotion.id = :promotionId
      order by s.studentNumber
      """)
  List<JStudent> findDetailedByPromotionId(@Param("promotionId") String promotionId);

  List<JStudent> findByCurrentGroupId(String groupId);

  @Query(
      """
      select s from JStudent s
      join fetch s.user
      join fetch s.promotion
      left join fetch s.currentGroup g
      left join fetch g.promotion
      where s.id = :id
      """)
  Optional<JStudent> findDetailedById(@Param("id") String id);

  @Query(
      """
      select distinct s from JStudent s
      join fetch s.user
      join fetch s.promotion
      left join fetch s.currentGroup g
      left join fetch g.promotion
      order by s.studentNumber
      """)
  List<JStudent> findAllDetailed();
}
