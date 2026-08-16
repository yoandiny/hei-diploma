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

  @Query(
      """
      select distinct e from JExam e
      join fetch e.course
      left join fetch e.groups
      where e.course.id = :courseId
      """)
  List<JExam> findDetailedByCourseId(@Param("courseId") String courseId);

  @Query(
      """
      select e from JExam e
      join fetch e.course
      left join fetch e.groups
      where e.id = :id
      """)
  Optional<JExam> findDetailedById(@Param("id") String id);
}
