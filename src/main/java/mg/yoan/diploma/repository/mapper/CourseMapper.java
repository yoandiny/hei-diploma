package mg.yoan.diploma.repository.mapper;

import java.util.UUID;
import mg.yoan.diploma.domain.Course;
import mg.yoan.diploma.repository.model.JCourse;

public class CourseMapper {

  private CourseMapper() {}

  public static Course toDomain(JCourse entity) {
    if (entity == null) {
      return null;
    }
    return Course.builder()
        .id(entity.getId() == null ? null : UUID.fromString(entity.getId()))
        .ref(entity.getRef())
        .title(entity.getTitle())
        .credits(entity.getCredits())
        .build();
  }

  public static JCourse toEntity(Course domain) {
    if (domain == null) {
      return null;
    }
    JCourse entity = new JCourse();
    entity.setId(domain.getId() == null ? null : domain.getId().toString());
    entity.setRef(domain.getRef());
    entity.setTitle(domain.getTitle());
    entity.setCredits(domain.getCredits());
    return entity;
  }
}
