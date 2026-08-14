package mg.yoan.diploma.repository.mapper;

import java.util.UUID;
import mg.yoan.diploma.domain.Teacher;
import mg.yoan.diploma.repository.model.JTeacher;

public class TeacherMapper {

  private TeacherMapper() {}

  public static Teacher toDomain(JTeacher entity) {
    if (entity == null) {
      return null;
    }
    return Teacher.builder()
        .id(entity.getId() != null ? UUID.fromString(entity.getId()) : null)
        .user(UserMapper.toDomain(entity.getUser()))
        .employeeNumber(entity.getEmployeeNumber())
        .build();
  }

  public static JTeacher toEntity(Teacher domain) {
    if (domain == null) {
      return null;
    }
    JTeacher entity = new JTeacher();
    entity.setId(domain.getId() != null ? domain.getId().toString() : null);
    entity.setUser(UserMapper.toEntity(domain.getUser()));
    entity.setEmployeeNumber(domain.getEmployeeNumber());
    return entity;
  }
}
