package mg.yoan.diploma.repository.mapper;

import java.util.UUID;
import mg.yoan.diploma.domain.Exam;
import mg.yoan.diploma.repository.model.JExam;

public class ExamMapper {

  private ExamMapper() {}

  public static Exam toDomain(JExam entity) {
    if (entity == null) {
      return null;
    }
    return Exam.builder()
        .id(UUID.fromString(entity.getId()))
        .course(CourseMapper.toDomain(entity.getCourse()))
        .dateExam(entity.getDateExam())
        .coefficient(entity.getCoefficient())
        .build();
  }

  public static JExam toEntity(Exam domain) {
    if (domain == null) {
      return null;
    }
    JExam entity = new JExam();
    entity.setId(domain.getId().toString());
    entity.setCourse(CourseMapper.toEntity(domain.getCourse()));
    entity.setDateExam(domain.getDateExam());
    entity.setCoefficient(domain.getCoefficient());
    return entity;
  }
}
