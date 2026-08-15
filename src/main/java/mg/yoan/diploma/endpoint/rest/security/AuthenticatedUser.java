package mg.yoan.diploma.endpoint.rest.security;

import java.io.Serializable;
import lombok.Getter;
import mg.yoan.diploma.domain.Role;

@Getter
public class AuthenticatedUser implements Serializable {

  private static final long serialVersionUID = 1L;

  private final String userId;
  private final String email;
  private final Role role;
  private final String firstName;
  private final String lastName;

  public AuthenticatedUser(String userId, String email, Role role) {
    this(userId, email, role, null, null);
  }

  public AuthenticatedUser(
      String userId, String email, Role role, String firstName, String lastName) {
    this.userId = userId;
    this.email = email;
    this.role = role;
    this.firstName = firstName;
    this.lastName = lastName;
  }

  public String getDisplayName() {
    String first = firstName == null ? "" : firstName;
    String last = lastName == null ? "" : lastName;
    String fullName = (first + " " + last).trim();
    return fullName.isEmpty() ? email : fullName;
  }

  public String getRoleLabel() {
    return switch (role) {
      case STUDENT -> "Étudiant";
      case TEACHER -> "Enseignant";
      case ADMIN -> "Administrateur";
    };
  }

  public boolean isAdmin() {
    return role == Role.ADMIN;
  }

  public boolean is(String otherUserId) {
    return userId.equals(otherUserId);
  }
}
