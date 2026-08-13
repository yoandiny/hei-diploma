package mg.yoan.diploma.domain;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Getter
@Builder
public class GradeHistory {
    private final UUID id;
    private final Grade grade;
    private final BigDecimal previousValue;
    private final BigDecimal newValue;
    private final String reason;
    private final User changedBy;
    private final Instant changedAt;
}
