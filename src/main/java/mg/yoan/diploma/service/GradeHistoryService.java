package mg.yoan.diploma.service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import mg.yoan.diploma.repository.JGradeHistoryRepository;
import mg.yoan.diploma.repository.model.JGrade;
import mg.yoan.diploma.repository.model.JGradeHistory;
import mg.yoan.diploma.repository.model.JUser;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class GradeHistoryService {

  private final JGradeHistoryRepository gradeHistoryRepository;

  public void logGradeChange(
      JGrade grade, BigDecimal previousValue, BigDecimal newValue, String reason, JUser changedBy) {
    JGradeHistory history = new JGradeHistory();
    history.setId(UUID.randomUUID().toString());
    history.setGrade(grade);
    history.setPreviousValue(previousValue);
    history.setNewValue(newValue);
    history.setReason(reason);
    history.setChangedBy(changedBy);
    history.setChangedAt(Instant.now());
    gradeHistoryRepository.save(history);
  }
}
