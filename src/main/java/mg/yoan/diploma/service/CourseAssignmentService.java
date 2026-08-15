package mg.yoan.diploma.service;

import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import mg.yoan.diploma.domain.CourseAssignment;
import mg.yoan.diploma.domain.Group;
import mg.yoan.diploma.repository.JCourseAssignmentRepository;
import mg.yoan.diploma.repository.JCourseRepository;
import mg.yoan.diploma.repository.JGroupRepository;
import mg.yoan.diploma.repository.JTeacherRepository;
import mg.yoan.diploma.repository.mapper.CourseAssignmentMapper;
import mg.yoan.diploma.repository.mapper.GroupMapper;
import mg.yoan.diploma.repository.model.JCourse;
import mg.yoan.diploma.repository.model.JCourseAssignment;
import mg.yoan.diploma.repository.model.JGroup;
import mg.yoan.diploma.repository.model.JTeacher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
public class CourseAssignmentService {

  private final JCourseAssignmentRepository assignmentRepository;
  private final JCourseRepository courseRepository;
  private final JTeacherRepository teacherRepository;
  private final JGroupRepository groupRepository;

  @Transactional(readOnly = true)
  public List<CourseAssignment> listByCourse(String courseId) {
    return assignmentRepository.findDetailedByCourseId(courseId).stream()
        .map(CourseAssignmentMapper::toDomain)
        .toList();
  }

  @Transactional(readOnly = true)
  public List<Group> listGroups() {
    return groupRepository.findAllDetailed().stream().map(GroupMapper::toDomain).toList();
  }

  @Transactional
  public void assign(String courseId, String teacherId, List<String> groupIds) {
    if (groupIds == null || groupIds.isEmpty()) {
      throw new DomainException("Choisissez au moins un groupe.");
    }
    JCourse course =
        courseRepository
            .findById(courseId)
            .orElseThrow(() -> new DomainException("Cours introuvable."));
    JTeacher teacher =
        teacherRepository
            .findDetailedById(teacherId)
            .orElseThrow(() -> new DomainException("Enseignant introuvable."));

    int created = 0;
    for (String groupId : groupIds) {
      if (groupId == null || groupId.isBlank()) {
        continue;
      }
      if (assignmentRepository.existsByCourseIdAndGroupIdAndTeacherId(
          courseId, groupId, teacherId)) {
        continue;
      }
      JGroup group =
          groupRepository
              .findById(groupId)
              .orElseThrow(() -> new DomainException("Groupe introuvable."));
      JCourseAssignment assignment = new JCourseAssignment();
      assignment.setId(UUID.randomUUID().toString());
      assignment.setCourse(course);
      assignment.setTeacher(teacher);
      assignment.setGroup(group);
      assignmentRepository.save(assignment);
      created++;
    }
    if (created == 0) {
      throw new DomainException("Cette affectation existe déjà pour le ou les groupes choisis.");
    }
  }

  @Transactional
  public void delete(String assignmentId) {
    if (!assignmentRepository.existsById(assignmentId)) {
      throw new DomainException("Affectation introuvable.");
    }
    assignmentRepository.deleteById(assignmentId);
  }
}
