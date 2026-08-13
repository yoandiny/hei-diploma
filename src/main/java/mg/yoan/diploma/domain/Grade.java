package mg.yoan.diploma.domain;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Getter
@Builder
public class Grade {
    private final UUID id;
    private final Exam exam;
    private final Student student;
    private final BigDecimal value;
    private final Teacher gradedBy;
    private final Instant gradedAt;

    public boolean belongsTo(UUID studentId) {
        return student.getId().equals(studentId);
    }
}
