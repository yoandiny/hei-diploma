package mg.yoan.diploma.service;

import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import mg.yoan.diploma.domain.Promotion;
import mg.yoan.diploma.repository.JGroupRepository;
import mg.yoan.diploma.repository.JPromotionRepository;
import mg.yoan.diploma.repository.mapper.PromotionMapper;
import mg.yoan.diploma.repository.model.JPromotion;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
public class PromotionService {

  private final JPromotionRepository promotionRepository;
  private final JGroupRepository groupRepository;

  @Transactional(readOnly = true)
  public List<Promotion> listAll() {
    return promotionRepository.findAllByOrderByStartYearDescLabelAsc().stream()
        .map(PromotionMapper::toDomain)
        .toList();
  }

  @Transactional(readOnly = true)
  public Promotion getById(String id) {
    return promotionRepository
        .findById(id)
        .map(PromotionMapper::toDomain)
        .orElseThrow(() -> new DomainException("Promotion introuvable."));
  }

  @Transactional
  public Promotion save(String id, String label, Integer startYear, Integer endYear) {
    String normalizedLabel = required(label, "Le libellé de la promotion est obligatoire.").trim();
    if (startYear == null || endYear == null) {
      throw new DomainException("Les années de début et de fin sont obligatoires.");
    }
    if (endYear < startYear) {
      throw new DomainException(
          "L'année de fin doit être postérieure ou égale à l'année de début.");
    }

    JPromotion entity;
    if (id == null || id.isBlank()) {
      entity = new JPromotion();
      entity.setId(UUID.randomUUID().toString());
    } else {
      entity =
          promotionRepository
              .findById(id)
              .orElseThrow(() -> new DomainException("Promotion introuvable."));
    }
    entity.setLabel(normalizedLabel);
    entity.setStartYear(startYear);
    entity.setEndYear(endYear);
    return PromotionMapper.toDomain(promotionRepository.save(entity));
  }

  @Transactional
  public void delete(String id) {
    if (!promotionRepository.existsById(id)) {
      throw new DomainException("Promotion introuvable.");
    }
    if (groupRepository.existsByPromotionId(id)) {
      throw new DomainException("Impossible de supprimer une promotion qui a encore des groupes.");
    }
    promotionRepository.deleteById(id);
  }

  private static String required(String value, String message) {
    if (value == null || value.isBlank()) {
      throw new DomainException(message);
    }
    return value;
  }
}
