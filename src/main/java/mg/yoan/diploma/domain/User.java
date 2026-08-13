package mg.yoan.diploma.domain;


import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

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
}
