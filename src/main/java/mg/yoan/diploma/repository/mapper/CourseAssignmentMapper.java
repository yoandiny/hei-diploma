package mg.yoan.diploma.repository.mapper;

import java.util.UUID;
import mg.yoan.diploma.domain.CourseAssignment;
import mg.yoan.diploma.repository.model.JCourseAssignment;

public class CourseAssignmentMapper {

  private CourseAssignmentMapper() {}

  public static CourseAssignment toDomain(JCourseAssignment entity) {
    if (entity == null) {
      return null;
    }
    return CourseAssignment.builder()
        .id(entity.getId() == null ? null : UUID.fromString(entity.getId()))
        .course(CourseMapper.toDomain(entity.getCourse()))
        .group(GroupMapper.toDomain(entity.getGroup()))
        .teacher(TeacherMapper.toDomain(entity.getTeacher()))
        .build();
  }

  public static JCourseAssignment toEntity(CourseAssignment domain) {
    if (domain == null) {
      return null;
    }
    JCourseAssignment entity = new JCourseAssignment();
    entity.setId(domain.getId().toString());
    entity.setCourse(CourseMapper.toEntity(domain.getCourse()));
    entity.setGroup(GroupMapper.toEntity(domain.getGroup()));
    entity.setTeacher(TeacherMapper.toEntity(domain.getTeacher()));
    return entity;
  }
}
