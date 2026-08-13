package mg.yoan.diploma.domain;

import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
public class Teacher {
    private final UUID id;
    private final User user;
    private final String employeeNumber;
}