package mg.yoan.diploma.domain;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Getter
@Builder
public class Exam {
    private final UUID id;
    private final Course course;
    private final Instant dateExam;
    private final BigDecimal coefficient;
}
