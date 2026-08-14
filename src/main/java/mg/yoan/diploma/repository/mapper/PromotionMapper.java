package mg.yoan.diploma.repository.mapper;

import mg.yoan.diploma.domain.Promotion;
import mg.yoan.diploma.repository.model.JPromotion;

public class PromotionMapper {

  private PromotionMapper() {}

  public static Promotion toDomain(JPromotion entity) {
    if (entity == null) {
      return null;
    }
    return Promotion.builder()
        .id(entity.getId() == null ? null : java.util.UUID.fromString(entity.getId()))
        .label(entity.getLabel())
        .startYear(entity.getStartYear())
        .endYear(entity.getEndYear())
        .build();
  }

  public static JPromotion toEntity(Promotion domain) {
    if (domain == null) {
      return null;
    }
    JPromotion entity = new JPromotion();
    entity.setId(domain.getId() == null ? null : domain.getId().toString());
    entity.setLabel(domain.getLabel());
    entity.setStartYear(domain.getStartYear());
    entity.setEndYear(domain.getEndYear());
    return entity;
  }
}
