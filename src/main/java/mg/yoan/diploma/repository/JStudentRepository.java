package mg.yoan.diploma.repository;

import java.util.Collection;
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

  boolean existsByStudentNumber(String studentNumber);

  long countByDeletedAtIsNull();

  List<JStudent> findByPromotionId(String promotionId);

  @Query(
      """
      select distinct s from JStudent s
      join fetch s.user
      join fetch s.promotion
      left join fetch s.currentGroup g
      left join fetch g.promotion
      where s.promotion.id = :promotionId
        and s.deletedAt is null
      order by s.studentNumber
      """)
  List<JStudent> findDetailedByPromotionId(@Param("promotionId") String promotionId);

  List<JStudent> findByCurrentGroupIdAndDeletedAtIsNull(String groupId);

  long countByCurrentGroupIdAndDeletedAtIsNull(String groupId);

  long countByCurrentGroupIdInAndDeletedAtIsNull(Collection<String> groupIds);

  @Query(
      """
      select distinct s from JStudent s
      join fetch s.user
      join fetch s.promotion
      left join fetch s.currentGroup g
      left join fetch g.promotion
      where s.currentGroup.id = :groupId
        and s.deletedAt is null
      order by s.studentNumber
      """)
  List<JStudent> findDetailedByCurrentGroupId(@Param("groupId") String groupId);

  @Query(
      """
      select distinct s from JStudent s
      join fetch s.user
      join fetch s.promotion
      join fetch s.currentGroup g
      left join fetch g.promotion
      where s.currentGroup.id in :groupIds
        and s.deletedAt is null
      order by g.ref, s.studentNumber
      """)
  List<JStudent> findDetailedByCurrentGroupIdIn(@Param("groupIds") Collection<String> groupIds);

  @Query(
      """
      select s from JStudent s
      join fetch s.user
      join fetch s.promotion
      left join fetch s.currentGroup g
      left join fetch g.promotion
      where s.id = :id
        and s.deletedAt is null
      """)
  Optional<JStudent> findDetailedById(@Param("id") String id);

  @Query(
      """
      select distinct s from JStudent s
      join fetch s.user
      join fetch s.promotion
      left join fetch s.currentGroup g
      left join fetch g.promotion
      where s.deletedAt is null
      order by s.studentNumber
      """)
  List<JStudent> findAllDetailed();

  @Query(
      """
select new mg.yoan.diploma.repository.JStudentRepository$GroupStudentCountDto(s.currentGroup.id, count(s))
from JStudent s
where s.currentGroup.id in :groupIds
  and s.deletedAt is null
group by s.currentGroup.id
""")
  List<GroupStudentCountDto> countByGroupIdIn(@Param("groupIds") Collection<String> groupIds);

  record GroupStudentCountDto(String groupId, long count) {}
}
