package mg.yoan.diploma.repository.mapper;

import java.util.UUID;
import mg.yoan.diploma.domain.Student;
import mg.yoan.diploma.repository.model.JStudent;

public class StudentMapper {

  private StudentMapper() {}

  public static Student toDomain(JStudent entity) {
    if (entity == null) {
      return null;
    }
    return Student.builder()
        .id(entity.getId() != null ? UUID.fromString(entity.getId()) : null)
        .user(UserMapper.toDomain(entity.getUser()))
        .studentNumber(entity.getStudentNumber())
        .promotion(PromotionMapper.toDomain(entity.getPromotion()))
        .currentGroup(GroupMapper.toDomain(entity.getCurrentGroup()))
        .build();
  }

  public static JStudent toEntity(Student domain) {
    if (domain == null) {
      return null;
    }
    JStudent entity = new JStudent();
    entity.setId(domain.getId() != null ? domain.getId().toString() : null);
    entity.setUser(UserMapper.toEntity(domain.getUser()));
    entity.setStudentNumber(domain.getStudentNumber());
    entity.setPromotion(PromotionMapper.toEntity(domain.getPromotion()));
    entity.setCurrentGroup(GroupMapper.toEntity(domain.getCurrentGroup()));
    return entity;
  }
}
