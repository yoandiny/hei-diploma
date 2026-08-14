package mg.yoan.diploma.domain;

import java.util.UUID;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class Promotion {
  private final UUID id;
  private final String label;
  private final int startYear;
  private final int endYear;
}
