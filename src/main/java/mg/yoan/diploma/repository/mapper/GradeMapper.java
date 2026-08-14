package mg.yoan.diploma.repository.mapper;

import java.util.UUID;
import mg.yoan.diploma.domain.Grade;
import mg.yoan.diploma.repository.model.JGrade;

public class GradeMapper {

  private GradeMapper() {}

  public static Grade toDomain(JGrade entity) {
    if (entity == null) {
      return null;
    }
    return Grade.builder()
        .id(entity.getId() == null ? null : UUID.fromString(entity.getId()))
        .exam(ExamMapper.toDomain(entity.getExam()))
        .student(StudentMapper.toDomain(entity.getStudent()))
        .value(entity.getValue())
        .gradedBy(TeacherMapper.toDomain(entity.getGradedBy()))
        .gradedAt(entity.getGradedAt())
        .build();
  }

  public static JGrade toEntity(Grade domain) {
    if (domain == null) {
      return null;
    }
    JGrade entity = new JGrade();
    entity.setId(domain.getId() == null ? null : domain.getId().toString());
    entity.setExam(ExamMapper.toEntity(domain.getExam()));
    entity.setStudent(StudentMapper.toEntity(domain.getStudent()));
    entity.setValue(domain.getValue());
    entity.setGradedBy(TeacherMapper.toEntity(domain.getGradedBy()));
    entity.setGradedAt(domain.getGradedAt());
    return entity;
  }
}
