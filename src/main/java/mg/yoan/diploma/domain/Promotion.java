package mg.yoan.diploma.domain;

import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
public class Promotion {
    private final UUID id;
    private final String label;
    private final int startYear;
    private final int endYear;
}
