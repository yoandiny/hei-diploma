package mg.yoan.diploma.endpoint.rest.security;

import lombok.Getter;
import mg.yoan.diploma.domain.Role;

@Getter
public class AuthenticatedUser {

  private final String userId;
  private final String email;
  private final Role role;

  public AuthenticatedUser(String userId, String email, Role role) {
    this.userId = userId;
    this.email = email;
    this.role = role;
  }

  public boolean isAdmin() {
    return role == Role.ADMIN;
  }

  public boolean is(String otherUserId) {
    return userId.equals(otherUserId);
  }
}
