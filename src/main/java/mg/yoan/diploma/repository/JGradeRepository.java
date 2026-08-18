package mg.yoan.diploma.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import mg.yoan.diploma.repository.model.JGrade;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface JGradeRepository extends JpaRepository<JGrade, String> {

  long countByExamId(String examId);

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

  @Query(
      """
      select g from JGrade g
      join fetch g.student
      where g.exam.id = :examId and g.student.id in :studentIds
      """)
  List<JGrade> findByExamIdAndStudentIdIn(
      @Param("examId") String examId, @Param("studentIds") Collection<String> studentIds);

  @Query(
      """
      select g from JGrade g
      join fetch g.exam e
      join fetch e.course
      join fetch g.student s
      left join fetch s.currentGroup
      join fetch s.user
      where g.id = :id
      """)
  Optional<JGrade> findDetailedById(@Param("id") String id);

  @Query(
      """
      select g from JGrade g
      join fetch g.exam e
      join fetch e.course
      join fetch g.student
      where g.student.id in :studentIds
      """)
  List<JGrade> findDetailedByStudentIdIn(@Param("studentIds") Collection<String> studentIds);

  @Query(
      """
      select new mg.yoan.diploma.repository.JGradeRepository$ExamGradeCountDto(g.exam.id, count(g))
      from JGrade g
      where g.exam.id in :examIds
      group by g.exam.id
      """)
  List<ExamGradeCountDto> countByExamIdIn(@Param("examIds") Collection<String> examIds);

  record ExamGradeCountDto(String examId, long count) {}
}
