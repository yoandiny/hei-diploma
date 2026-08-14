package mg.yoan.diploma.domain;

import java.util.UUID;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class Teacher {
  private final UUID id;
  private final User user;
  private final String employeeNumber;
}
