package mg.yoan.diploma.domain;

import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
public class Course {
    private final UUID id;
    private final String ref;
    private final String title;
    private final int credits;
}
