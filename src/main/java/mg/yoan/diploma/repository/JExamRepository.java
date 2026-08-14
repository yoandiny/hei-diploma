package mg.yoan.diploma.repository;

import java.util.List;
import mg.yoan.diploma.repository.model.JExam;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JExamRepository extends JpaRepository<JExam, String> {

  List<JExam> findByCourseId(String courseId);
}
