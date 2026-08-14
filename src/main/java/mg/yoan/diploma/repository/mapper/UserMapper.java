package mg.yoan.diploma.repository.mapper;

import java.util.UUID;
import mg.yoan.diploma.domain.User;
import mg.yoan.diploma.repository.model.JUser;

public class UserMapper {

  private UserMapper() {}

  public static User toDomain(JUser entity) {
    if (entity == null) {
      return null;
    }
    return User.builder()
        .id(entity.getId() == null ? null : UUID.fromString(entity.getId()))
        .email(entity.getEmail())
        .firstName(entity.getFirstName())
        .lastName(entity.getLastName())
        .role(entity.getRole())
        .enabled(entity.isEnabled())
        .createdAt(entity.getCreatedAt())
        .build();
  }

  public static JUser toEntity(User domain) {
    if (domain == null) {
      return null;
    }
    JUser entity = new JUser();
    entity.setId(domain.getId() == null ? null : domain.getId().toString());
    entity.setEmail(domain.getEmail());
    entity.setFirstName(domain.getFirstName());
    entity.setLastName(domain.getLastName());
    entity.setRole(domain.getRole());
    entity.setEnabled(domain.isEnabled());
    entity.setCreatedAt(domain.getCreatedAt());
    return entity;
  }
}
