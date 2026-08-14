package mg.yoan.diploma.repository.mapper;

import java.util.UUID;
import mg.yoan.diploma.domain.Group;
import mg.yoan.diploma.repository.model.JGroup;

public class GroupMapper {

  private GroupMapper() {}

  public static Group toDomain(JGroup entity) {
    if (entity == null) {
      return null;
    }
    return Group.builder()
        .id(entity.getId() == null ? null : UUID.fromString(entity.getId()))
        .ref(entity.getRef())
        .promotion(PromotionMapper.toDomain(entity.getPromotion()))
        .build();
  }

  public static JGroup toEntity(Group domain) {
    if (domain == null) {
      return null;
    }
    JGroup entity = new JGroup();
    entity.setId(domain.getId() == null ? null : domain.getId().toString());
    entity.setRef(domain.getRef());
    entity.setPromotion(PromotionMapper.toEntity(domain.getPromotion()));
    return entity;
  }
}
