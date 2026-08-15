package mg.yoan.diploma.service;

import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import mg.yoan.diploma.domain.Group;
import mg.yoan.diploma.repository.JCourseAssignmentRepository;
import mg.yoan.diploma.repository.JGroupRepository;
import mg.yoan.diploma.repository.JPromotionRepository;
import mg.yoan.diploma.repository.JStudentGroupHistoryRepository;
import mg.yoan.diploma.repository.JStudentRepository;
import mg.yoan.diploma.repository.mapper.GroupMapper;
import mg.yoan.diploma.repository.model.JGroup;
import mg.yoan.diploma.repository.model.JPromotion;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
public class GroupService {

  private final JGroupRepository groupRepository;
  private final JPromotionRepository promotionRepository;
  private final JCourseAssignmentRepository assignmentRepository;
  private final JStudentRepository studentRepository;
  private final JStudentGroupHistoryRepository groupHistoryRepository;

  @Transactional(readOnly = true)
  public List<Group> listAll() {
    return groupRepository.findAllDetailed().stream().map(GroupMapper::toDomain).toList();
  }

  @Transactional(readOnly = true)
  public Group getById(String id) {
    return groupRepository
        .findDetailedById(id)
        .map(GroupMapper::toDomain)
        .orElseThrow(() -> new DomainException("Groupe introuvable."));
  }

  @Transactional
  public Group save(String id, String ref, String promotionId) {
    String normalizedRef = required(ref, "La référence du groupe est obligatoire.").trim();
    JPromotion promotion =
        promotionRepository
            .findById(required(promotionId, "La promotion est obligatoire."))
            .orElseThrow(() -> new DomainException("Promotion introuvable."));

    boolean duplicate =
        id == null || id.isBlank()
            ? groupRepository.existsByRefIgnoreCaseAndPromotionId(normalizedRef, promotion.getId())
            : groupRepository.existsByRefIgnoreCaseAndPromotionIdAndIdNot(
                normalizedRef, promotion.getId(), id);
    if (duplicate) {
      throw new DomainException("Ce groupe existe déjà dans cette promotion.");
    }

    JGroup entity;
    if (id == null || id.isBlank()) {
      entity = new JGroup();
      entity.setId(UUID.randomUUID().toString());
    } else {
      entity =
          groupRepository
              .findDetailedById(id)
              .orElseThrow(() -> new DomainException("Groupe introuvable."));
    }
    entity.setRef(normalizedRef);
    entity.setPromotion(promotion);
    return GroupMapper.toDomain(groupRepository.save(entity));
  }

  @Transactional
  public void delete(String id) {
    if (!groupRepository.existsById(id)) {
      throw new DomainException("Groupe introuvable.");
    }
    if (!studentRepository.findByCurrentGroupId(id).isEmpty()) {
      throw new DomainException("Impossible de supprimer un groupe qui a encore des étudiants.");
    }
    if (assignmentRepository.existsByGroupId(id)) {
      throw new DomainException("Impossible de supprimer un groupe encore affecté à un cours.");
    }
    if (groupHistoryRepository.existsByGroupId(id)) {
      throw new DomainException("Impossible de supprimer un groupe présent dans l'historique.");
    }
    groupRepository.deleteById(id);
  }

  private static String required(String value, String message) {
    if (value == null || value.isBlank()) {
      throw new DomainException(message);
    }
    return value;
  }
}
