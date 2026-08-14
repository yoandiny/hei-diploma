package mg.yoan.diploma.repository;

import java.util.List;
import java.util.Optional;
import mg.yoan.diploma.repository.model.JGrade;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JGradeRepository extends JpaRepository<JGrade, String> {

  List<JGrade> findByStudentId(String studentId);

  List<JGrade> findByExamId(String examId);

  Optional<JGrade> findByExamIdAndStudentId(String examId, String studentId);
}
