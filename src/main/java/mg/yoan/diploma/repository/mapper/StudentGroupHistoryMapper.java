package mg.yoan.diploma.repository.mapper;

import java.util.UUID;
import mg.yoan.diploma.domain.StudentGroupHistory;
import mg.yoan.diploma.repository.model.JStudentGroupHistory;

public class StudentGroupHistoryMapper {

  private StudentGroupHistoryMapper() {}

  public static StudentGroupHistory toDomain(JStudentGroupHistory entity) {
    if (entity == null) {
      return null;
    }
    return StudentGroupHistory.builder()
        .id(entity.getId() == null ? null : UUID.fromString(entity.getId()))
        .student(StudentMapper.toDomain(entity.getStudent()))
        .group(GroupMapper.toDomain(entity.getGroup()))
        .startDate(entity.getStartDate())
        .endDate(entity.getEndDate())
        .build();
  }

  public static JStudentGroupHistory toEntity(StudentGroupHistory domain) {
    if (domain == null) {
      return null;
    }
    JStudentGroupHistory entity = new JStudentGroupHistory();
    entity.setId(domain.getId() == null ? null : domain.getId().toString());
    entity.setStudent(StudentMapper.toEntity(domain.getStudent()));
    entity.setGroup(GroupMapper.toEntity(domain.getGroup()));
    entity.setStartDate(domain.getStartDate());
    entity.setEndDate(domain.getEndDate());
    return entity;
  }
}
