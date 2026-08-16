package mg.yoan.diploma.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import mg.yoan.diploma.domain.Grade;
import mg.yoan.diploma.domain.Student;
import mg.yoan.diploma.domain.Transcript;
import mg.yoan.diploma.repository.JCourseAssignmentRepository;
import mg.yoan.diploma.repository.JGradeRepository;
import mg.yoan.diploma.repository.JPromotionRepository;
import mg.yoan.diploma.repository.JStudentGroupHistoryRepository;
import mg.yoan.diploma.repository.JStudentRepository;
import mg.yoan.diploma.repository.mapper.ExamMapper;
import mg.yoan.diploma.repository.mapper.StudentMapper;
import mg.yoan.diploma.repository.model.JCourseAssignment;
import mg.yoan.diploma.repository.model.JGrade;
import mg.yoan.diploma.repository.model.JStudent;
import mg.yoan.diploma.repository.model.JStudentGroupHistory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
public class GraduationService {

  private final JPromotionRepository promotionRepository;
  private final JStudentRepository studentRepository;
  private final JStudentGroupHistoryRepository studentGroupHistoryRepository;
  private final JCourseAssignmentRepository courseAssignmentRepository;
  private final JGradeRepository gradeRepository;

  @Transactional(readOnly = true)
  public List<GraduatedStudent> listGraduates(String promotionId) {
    if (!promotionRepository.existsById(promotionId)) {
      throw new DomainException("Promotion introuvable.");
    }

    List<JStudent> studentEntities = studentRepository.findDetailedByPromotionId(promotionId);
    if (studentEntities.isEmpty()) {
      return List.of();
    }

    List<String> studentIds = studentEntities.stream().map(JStudent::getId).toList();
    Map<String, Set<String>> groupIdsByStudent = groupIdsByStudent(studentEntities, studentIds);
    Map<String, List<JCourseAssignment>> assignmentsByGroup =
        courseAssignmentRepository.findByPromotionId(promotionId).stream()
            .collect(Collectors.groupingBy(assignment -> assignment.getGroup().getId()));
    Map<String, List<JGrade>> gradesByStudent = new HashMap<>();
    for (JGrade grade : gradeRepository.findDetailedByStudentIdIn(studentIds)) {
      gradesByStudent
          .computeIfAbsent(grade.getStudent().getId(), key -> new ArrayList<>())
          .add(grade);
    }

    List<GraduatedStudent> graduates = new ArrayList<>();
    for (JStudent entity : studentEntities) {
      Student student = StudentMapper.toDomain(entity);
      List<Grade> grades =
          gradesByStudent.getOrDefault(entity.getId(), List.of()).stream()
              .map(grade -> toGrade(grade, student))
              .toList();
      Transcript transcript = Transcript.builder().student(student).grades(grades).build();
      Set<UUID> requiredCourseIds =
          requiredCourseIds(entity.getId(), groupIdsByStudent, assignmentsByGroup);
      if (transcript.isGraduated(requiredCourseIds)) {
        graduates.add(new GraduatedStudent(student, transcript.weightedAverage()));
      }
    }
    return graduates;
  }

  private Map<String, Set<String>> groupIdsByStudent(
      List<JStudent> studentEntities, List<String> studentIds) {
    Map<String, Set<String>> groupIdsByStudent = new HashMap<>();
    for (JStudent student : studentEntities) {
      Set<String> groupIds = new HashSet<>();
      if (student.getCurrentGroup() != null) {
        groupIds.add(student.getCurrentGroup().getId());
      }
      groupIdsByStudent.put(student.getId(), groupIds);
    }
    for (JStudentGroupHistory history :
        studentGroupHistoryRepository.findByStudentIdIn(studentIds)) {
      groupIdsByStudent
          .computeIfAbsent(history.getStudent().getId(), key -> new HashSet<>())
          .add(history.getGroup().getId());
    }
    return groupIdsByStudent;
  }

  private static Set<UUID> requiredCourseIds(
      String studentId,
      Map<String, Set<String>> groupIdsByStudent,
      Map<String, List<JCourseAssignment>> assignmentsByGroup) {
    Set<UUID> courseIds = new HashSet<>();
    for (String groupId : groupIdsByStudent.getOrDefault(studentId, Set.of())) {
      for (JCourseAssignment assignment : assignmentsByGroup.getOrDefault(groupId, List.of())) {
        courseIds.add(UUID.fromString(assignment.getCourse().getId()));
      }
    }
    return courseIds;
  }

  private static Grade toGrade(JGrade entity, Student student) {
    return Grade.builder()
        .id(UUID.fromString(entity.getId()))
        .exam(ExamMapper.toDomain(entity.getExam()))
        .student(student)
        .value(entity.getValue())
        .gradedAt(entity.getGradedAt())
        .build();
  }

  public record GraduatedStudent(Student student, BigDecimal weightedAverage) {}
}
