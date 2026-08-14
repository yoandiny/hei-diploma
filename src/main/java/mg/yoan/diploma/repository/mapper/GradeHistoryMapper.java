package mg.yoan.diploma.repository.mapper;

import java.util.UUID;
import mg.yoan.diploma.domain.GradeHistory;
import mg.yoan.diploma.repository.model.JGradeHistory;

public class GradeHistoryMapper {

  private GradeHistoryMapper() {}

  public static GradeHistory toDomain(JGradeHistory entity) {
    if (entity == null) {
      return null;
    }
    return GradeHistory.builder()
        .id(entity.getId() == null ? null : UUID.fromString(entity.getId()))
        .grade(GradeMapper.toDomain(entity.getGrade()))
        .previousValue(entity.getPreviousValue())
        .newValue(entity.getNewValue())
        .reason(entity.getReason())
        .changedBy(UserMapper.toDomain(entity.getChangedBy()))
        .changedAt(entity.getChangedAt())
        .build();
  }

  public static JGradeHistory toEntity(GradeHistory domain) {
    if (domain == null) {
      return null;
    }
    JGradeHistory entity = new JGradeHistory();
    entity.setId(domain.getId() == null ? null : domain.getId().toString());
    entity.setGrade(GradeMapper.toEntity(domain.getGrade()));
    entity.setPreviousValue(domain.getPreviousValue());
    entity.setNewValue(domain.getNewValue());
    entity.setReason(domain.getReason());
    entity.setChangedBy(UserMapper.toEntity(domain.getChangedBy()));
    entity.setChangedAt(domain.getChangedAt());
    return entity;
  }
}
