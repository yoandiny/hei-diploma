package mg.yoan.diploma.repository;

import java.util.List;
import mg.yoan.diploma.repository.model.JGradeHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface JGradeHistoryRepository extends JpaRepository<JGradeHistory, String> {

  List<JGradeHistory> findByGradeIdOrderByChangedAtDesc(String gradeId);

  @Query(
      """
      select h from JGradeHistory h
      join fetch h.changedBy
      join fetch h.grade g
      join fetch g.exam e
      join fetch e.course
      join fetch g.student s
      join fetch s.user
      left join fetch s.currentGroup
      where h.grade.id = :gradeId
      order by h.changedAt desc
      """)
  List<JGradeHistory> findDetailedByGradeId(@Param("gradeId") String gradeId);
}
