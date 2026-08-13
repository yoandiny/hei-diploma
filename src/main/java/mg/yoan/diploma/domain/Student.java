package mg.yoan.diploma.domain;

import java.util.UUID;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class Student {
  private final UUID id;
  private final User user;
  private final String studentNumber;
  private final Promotion promotion;
  private final Group currentGroup;

  public boolean isSelf(UUID requesterId) {
    return this.id.equals(requesterId);
  }
}
