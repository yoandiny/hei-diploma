package mg.yoan.diploma.domain;

import java.util.UUID;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class Group {
  private final UUID id;
  private final String ref;
  private final Promotion promotion;
}
