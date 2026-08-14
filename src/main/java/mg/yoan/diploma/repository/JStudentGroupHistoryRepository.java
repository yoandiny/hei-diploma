package mg.yoan.diploma.repository;

import java.util.List;
import java.util.Optional;
import mg.yoan.diploma.repository.model.JStudentGroupHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JStudentGroupHistoryRepository
    extends JpaRepository<JStudentGroupHistory, String> {

  List<JStudentGroupHistory> findByStudentIdOrderByStartDateDesc(String studentId);

  Optional<JStudentGroupHistory> findByStudentIdAndEndDateIsNull(String studentId);
}
