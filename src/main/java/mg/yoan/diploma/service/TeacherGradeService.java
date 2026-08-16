package mg.yoan.diploma.service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.AllArgsConstructor;
import mg.yoan.diploma.domain.CourseAssignment;
import mg.yoan.diploma.domain.Exam;
import mg.yoan.diploma.repository.JCourseAssignmentRepository;
import mg.yoan.diploma.repository.JCourseRepository;
import mg.yoan.diploma.repository.JExamRepository;
import mg.yoan.diploma.repository.JGradeHistoryRepository;
import mg.yoan.diploma.repository.JGradeRepository;
import mg.yoan.diploma.repository.JGroupRepository;
import mg.yoan.diploma.repository.JStudentRepository;
import mg.yoan.diploma.repository.JTeacherRepository;
import mg.yoan.diploma.repository.JUserRepository;
import mg.yoan.diploma.repository.mapper.CourseAssignmentMapper;
import mg.yoan.diploma.repository.mapper.ExamMapper;
import mg.yoan.diploma.repository.model.JExam;
import mg.yoan.diploma.repository.model.JGrade;
import mg.yoan.diploma.repository.model.JGradeHistory;
import mg.yoan.diploma.repository.model.JStudent;
import mg.yoan.diploma.repository.model.JUser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
public class TeacherGradeService {

  private static final BigDecimal MIN_GRADE = BigDecimal.ZERO;
  private static final BigDecimal MAX_GRADE = new BigDecimal("20");

  private final JCourseAssignmentRepository assignmentRepository;
  private final JCourseRepository courseRepository;
  private final JExamRepository examRepository;
  private final JGradeRepository gradeRepository;
  private final JGradeHistoryRepository gradeHistoryRepository;
  private final JGroupRepository groupRepository;
  private final JStudentRepository studentRepository;
  private final JTeacherRepository teacherRepository;
  private final JUserRepository userRepository;

  @Transactional(readOnly = true)
  public List<AssignedCourse> listAssignedCourses(String teacherId) {
    Map<String, AssignedCourse> byCourse = new LinkedHashMap<>();
    for (CourseAssignment assignment :
        assignmentRepository.findDetailedByTeacherId(teacherId).stream()
            .map(CourseAssignmentMapper::toDomain)
            .toList()) {
      var course = assignment.getCourse();
      var group = assignment.getGroup();
      AssignedCourse existing =
          byCourse.computeIfAbsent(
              course.getId().toString(),
              key ->
                  new AssignedCourse(
                      course.getId().toString(),
                      course.getRef(),
                      course.getTitle(),
                      new ArrayList<>(),
                      0));
      int students = (int) studentRepository.countByCurrentGroupId(group.getId().toString());
      existing.groups().add(new AssignedGroup(group.getId().toString(), group.getRef(), students));
    }
    return byCourse.values().stream()
        .map(
            course ->
                new AssignedCourse(
                    course.courseId(),
                    course.ref(),
                    course.title(),
                    course.groups(),
                    course.groups().stream().mapToInt(AssignedGroup::studentCount).sum()))
        .toList();
  }

  @Transactional(readOnly = true)
  public List<Exam> listExams(String teacherId, String courseId) {
    assertTeachesCourse(teacherId, courseId);
    return examRepository.findByCourseIdOrderByDateExamDesc(courseId).stream()
        .map(ExamMapper::toDomain)
        .toList();
  }

  @Transactional
  public Exam createExam(
      String teacherId, String courseId, Instant dateExam, BigDecimal coefficient) {
    assertTeachesCourse(teacherId, courseId);
    if (dateExam == null) {
      throw new DomainException("La date de l'examen est obligatoire.");
    }
    if (coefficient == null || coefficient.compareTo(BigDecimal.ZERO) <= 0) {
      throw new DomainException("Le coefficient doit être supérieur à 0.");
    }
    var course =
        courseRepository
            .findById(courseId)
            .orElseThrow(() -> new DomainException("Cours introuvable."));
    JExam exam = new JExam();
    exam.setId(UUID.randomUUID().toString());
    exam.setCourse(course);
    exam.setDateExam(dateExam);
    exam.setCoefficient(coefficient);
    return ExamMapper.toDomain(examRepository.save(exam));
  }

  @Transactional(readOnly = true)
  public GradeSheet getGradeSheet(
      String teacherId, String courseId, String groupId, String examId) {
    assertResponsible(teacherId, courseId, groupId);
    JExam exam =
        examRepository
            .findDetailedById(examId)
            .orElseThrow(() -> new DomainException("Examen introuvable."));
    if (!exam.getCourse().getId().equals(courseId)) {
      throw new DomainException("Cet examen n'appartient pas à ce cours.");
    }
    List<JStudent> students = studentRepository.findDetailedByCurrentGroupId(groupId);
    List<String> studentIds = students.stream().map(JStudent::getId).toList();
    Map<String, JGrade> gradesByStudent = new LinkedHashMap<>();
    if (!studentIds.isEmpty()) {
      for (JGrade grade : gradeRepository.findByExamIdAndStudentIdIn(examId, studentIds)) {
        gradesByStudent.put(grade.getStudent().getId(), grade);
      }
    }
    var group =
        groupRepository
            .findById(groupId)
            .orElseThrow(() -> new DomainException("Groupe introuvable."));
    String groupRef = group.getRef();
    List<GradeRow> rows = new ArrayList<>();
    for (JStudent student : students) {
      JGrade grade = gradesByStudent.get(student.getId());
      var user = student.getUser();
      rows.add(
          new GradeRow(
              student.getId(),
              student.getStudentNumber(),
              fullName(user),
              groupRef,
              grade == null ? null : grade.getId(),
              grade == null ? null : grade.getValue()));
    }
    return new GradeSheet(ExamMapper.toDomain(exam), courseId, groupId, groupRef, rows);
  }

  @Transactional
  public void saveGrade(
      String teacherId,
      String courseId,
      String groupId,
      String examId,
      String studentId,
      BigDecimal value,
      String reason) {
    assertResponsible(teacherId, courseId, groupId);
    String normalizedReason = required(reason, "Le motif est obligatoire.").trim();
    if (value == null || value.compareTo(MIN_GRADE) < 0 || value.compareTo(MAX_GRADE) > 0) {
      throw new DomainException("La note doit être comprise entre 0 et 20.");
    }
    JExam exam =
        examRepository
            .findDetailedById(examId)
            .orElseThrow(() -> new DomainException("Examen introuvable."));
    if (!exam.getCourse().getId().equals(courseId)) {
      throw new DomainException("Cet examen n'appartient pas à ce cours.");
    }
    JStudent student =
        studentRepository
            .findDetailedById(studentId)
            .orElseThrow(() -> new DomainException("Étudiant introuvable."));
    if (student.getCurrentGroup() == null || !groupId.equals(student.getCurrentGroup().getId())) {
      throw new DomainException("Cet étudiant n'appartient pas à ce groupe.");
    }
    var teacher =
        teacherRepository
            .findById(teacherId)
            .orElseThrow(() -> new DomainException("Enseignant introuvable."));
    JUser changedBy =
        userRepository
            .findById(teacherId)
            .orElseThrow(() -> new DomainException("Utilisateur introuvable."));

    JGrade grade =
        gradeRepository
            .findByExamIdAndStudentId(examId, studentId)
            .orElseGet(
                () -> {
                  JGrade created = new JGrade();
                  created.setId(UUID.randomUUID().toString());
                  created.setExam(exam);
                  created.setStudent(student);
                  return created;
                });
    BigDecimal previous = grade.getValue();
    if (previous != null && previous.compareTo(value) == 0) {
      throw new DomainException("La note n'a pas changé.");
    }
    grade.setValue(value);
    grade.setGradedBy(teacher);
    grade.setGradedAt(Instant.now());
    JGrade saved = gradeRepository.save(grade);

    JGradeHistory history = new JGradeHistory();
    history.setId(UUID.randomUUID().toString());
    history.setGrade(saved);
    history.setPreviousValue(previous);
    history.setNewValue(value);
    history.setReason(normalizedReason);
    history.setChangedBy(changedBy);
    history.setChangedAt(Instant.now());
    gradeHistoryRepository.save(history);
  }

  @Transactional(readOnly = true)
  public GradeHistoryView getHistory(String teacherId, String gradeId) {
    var histories = gradeHistoryRepository.findDetailedByGradeId(gradeId);
    if (histories.isEmpty()) {
      JGrade grade =
          gradeRepository
              .findDetailedById(gradeId)
              .orElseThrow(() -> new DomainException("Note introuvable."));
      String groupId =
          grade.getStudent().getCurrentGroup() == null
              ? null
              : grade.getStudent().getCurrentGroup().getId();
      if (groupId == null
          || !assignmentRepository.existsByCourseIdAndGroupIdAndTeacherId(
              grade.getExam().getCourse().getId(), groupId, teacherId)) {
        throw new DomainException("Vous n'êtes pas responsable de cette note.");
      }
      return new GradeHistoryView(grade, List.of());
    }
    JGrade grade = histories.get(0).getGrade();
    String groupId =
        grade.getStudent().getCurrentGroup() == null
            ? null
            : grade.getStudent().getCurrentGroup().getId();
    if (groupId == null
        || !assignmentRepository.existsByCourseIdAndGroupIdAndTeacherId(
            grade.getExam().getCourse().getId(), groupId, teacherId)) {
      throw new DomainException("Vous n'êtes pas responsable de cette note.");
    }
    List<HistoryRow> rows =
        histories.stream()
            .map(
                history ->
                    new HistoryRow(
                        history.getChangedAt(),
                        fullName(history.getChangedBy()),
                        history.getPreviousValue(),
                        history.getNewValue(),
                        history.getReason()))
            .toList();
    return new GradeHistoryView(grade, rows);
  }

  private void assertTeachesCourse(String teacherId, String courseId) {
    if (!assignmentRepository.existsByCourseIdAndTeacherId(courseId, teacherId)) {
      throw new DomainException("Vous n'êtes pas responsable de ce cours.");
    }
  }

  private void assertResponsible(String teacherId, String courseId, String groupId) {
    if (!assignmentRepository.existsByCourseIdAndGroupIdAndTeacherId(
        courseId, groupId, teacherId)) {
      throw new DomainException("Vous n'êtes pas responsable de ce cours pour ce groupe.");
    }
  }

  private static String required(String value, String message) {
    if (value == null || value.isBlank()) {
      throw new DomainException(message);
    }
    return value;
  }

  private static String fullName(JUser user) {
    if (user == null) {
      return "";
    }
    String first = user.getFirstName() == null ? "" : user.getFirstName();
    String last = user.getLastName() == null ? "" : user.getLastName();
    return (first + " " + last).trim();
  }

  public record AssignedGroup(String groupId, String ref, int studentCount) {}

  public record AssignedCourse(
      String courseId, String ref, String title, List<AssignedGroup> groups, int studentCount) {}

  public record GradeRow(
      String studentId,
      String studentNumber,
      String fullName,
      String groupRef,
      String gradeId,
      BigDecimal currentValue) {}

  public record GradeSheet(
      Exam exam, String courseId, String groupId, String groupRef, List<GradeRow> rows) {}

  public record HistoryRow(
      Instant changedAt,
      String changedByName,
      BigDecimal previousValue,
      BigDecimal newValue,
      String reason) {}

  public record GradeHistoryView(JGrade grade, List<HistoryRow> entries) {}
}
