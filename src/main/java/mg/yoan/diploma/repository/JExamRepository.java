package mg.yoan.diploma.repository;

import java.util.List;
import java.util.Optional;
import mg.yoan.diploma.repository.model.JExam;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface JExamRepository extends JpaRepository<JExam, String> {

  List<JExam> findByCourseId(String courseId);

  List<JExam> findByCourseIdOrderByDateExamDesc(String courseId);

  @Query(
      """
      select e from JExam e
      join fetch e.course
      where e.id = :id
      """)
  Optional<JExam> findDetailedById(@Param("id") String id);
}
