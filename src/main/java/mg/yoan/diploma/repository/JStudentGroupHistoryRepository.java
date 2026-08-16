package mg.yoan.diploma.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import mg.yoan.diploma.repository.model.JStudentGroupHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface JStudentGroupHistoryRepository
    extends JpaRepository<JStudentGroupHistory, String> {

  List<JStudentGroupHistory> findByStudentIdOrderByStartDateDesc(String studentId);

  Optional<JStudentGroupHistory> findByStudentIdAndEndDateIsNull(String studentId);

  boolean existsByGroupId(String groupId);

  @Query(
      """
      select h from JStudentGroupHistory h
      join fetch h.group
      join fetch h.student
      where h.student.id in :studentIds
      """)
  List<JStudentGroupHistory> findByStudentIdIn(@Param("studentIds") Collection<String> studentIds);
}
