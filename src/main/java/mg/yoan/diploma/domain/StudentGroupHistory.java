package mg.yoan.diploma.domain;

import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Builder
public class StudentGroupHistory {
    private final UUID id;
    private final Student student;
    private final Group group;
    private final Instant startDate;
    private final Instant endDate;

    public boolean isActive() {
        return endDate == null;
    }
}
