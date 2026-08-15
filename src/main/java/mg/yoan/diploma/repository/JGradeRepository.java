package mg.yoan.diploma.repository;

import java.util.List;
import java.util.Optional;
import mg.yoan.diploma.repository.model.JGrade;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface JGradeRepository extends JpaRepository<JGrade, String> {

  List<JGrade> findByStudentId(String studentId);

  List<JGrade> findByExamId(String examId);

  Optional<JGrade> findByExamIdAndStudentId(String examId, String studentId);

  @Query(
      """
      select g from JGrade g
      join fetch g.exam e
      join fetch e.course
      where g.student.id = :studentId
      order by e.dateExam asc
      """)
  List<JGrade> findDetailedByStudentId(@Param("studentId") String studentId);
}
