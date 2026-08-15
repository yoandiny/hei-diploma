package mg.yoan.diploma.domain;

import java.time.Instant;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class User {
  private final UUID id;
  private final String email;
  private final String firstName;
  private final String lastName;
  private final Role role;
  private final boolean enabled;
  private final Instant createdAt;

  public String getFullName() {
    String first = firstName == null ? "" : firstName;
    String last = lastName == null ? "" : lastName;
    return (first + " " + last).trim();
  }
}
