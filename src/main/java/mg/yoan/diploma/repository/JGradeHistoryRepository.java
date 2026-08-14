package mg.yoan.diploma.repository;

import java.util.List;
import mg.yoan.diploma.repository.model.JGradeHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JGradeHistoryRepository extends JpaRepository<JGradeHistory, String> {

  List<JGradeHistory> findByGradeIdOrderByChangedAtDesc(String gradeId);
}
