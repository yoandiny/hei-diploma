package mg.yoan.diploma.service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.AllArgsConstructor;
import mg.yoan.diploma.repository.JCourseRepository;
import mg.yoan.diploma.repository.JExamRepository;
import mg.yoan.diploma.repository.JGradeHistoryRepository;
import mg.yoan.diploma.repository.JGradeRepository;
import mg.yoan.diploma.repository.JGroupRepository;
import mg.yoan.diploma.repository.JStudentRepository;
import mg.yoan.diploma.repository.JTeacherRepository;
import mg.yoan.diploma.repository.JUserRepository;
import mg.yoan.diploma.repository.model.JExam;
import mg.yoan.diploma.repository.model.JGrade;
import mg.yoan.diploma.repository.model.JGroup;
import mg.yoan.diploma.repository.model.JStudent;
import mg.yoan.diploma.repository.model.JTeacher;
import mg.yoan.diploma.repository.model.JUser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
public class AdminGradeService {

  private static final BigDecimal MIN_GRADE = BigDecimal.ZERO;
  private static final BigDecimal GRADE_SCALE = new BigDecimal("20");

  private final JCourseRepository courseRepository;
  private final JExamRepository examRepository;
  private final GradeHistoryService gradeHistoryService;
  private final JGradeRepository gradeRepository;
  private final JGradeHistoryRepository gradeHistoryRepository;
  private final JGroupRepository groupRepository;
  private final JStudentRepository studentRepository;
  private final JTeacherRepository teacherRepository;
  private final JUserRepository userRepository;

  @Transactional(readOnly = true)
  public AdminHome loadHome() {
    List<CourseOption> courses = listAllCourses();
    int examCount = (int) examRepository.count();
    int gradeCount = (int) gradeRepository.count();
    return new AdminHome(courses, examCount, gradeCount);
  }

  @Transactional(readOnly = true)
  public List<CourseOption> listAllCourses() {
    return courseRepository.findAll().stream()
        .map(
            course ->
                new CourseOption(
                    course.getId(), course.getRef(), course.getTitle(), course.getCredits()))
        .sorted(Comparator.comparing(CourseOption::ref))
        .toList();
  }

  private JTeacher getDefaultTeacher() {
    return teacherRepository.findAll().stream().findFirst().orElse(null);
  }

  @Transactional(readOnly = true)
  public List<ExamOption> listExamsByCourse(String courseId) {
    courseRepository
        .findById(courseId)
        .orElseThrow(() -> new DomainException("Cours introuvable."));

    return examRepository.findDetailedByCourseId(courseId).stream()
        .sorted(Comparator.comparing(JExam::getDateExam).reversed())
        .map(this::toExamOption)
        .toList();
  }

  @Transactional(readOnly = true)
  public List<GroupOption> listGroupsByExam(String examId) {
    JExam exam =
        examRepository
            .findDetailedById(examId)
            .orElseThrow(() -> new DomainException("Examen introuvable."));

    return exam.getGroups().stream()
        .sorted(Comparator.comparing(JGroup::getRef, String.CASE_INSENSITIVE_ORDER))
        .map(group -> new GroupOption(group.getId(), group.getRef()))
        .toList();
  }

  @Transactional(readOnly = true)
  public GradeSheet getGradeSheet(String examId, String groupId) {
    JExam exam =
        examRepository
            .findDetailedById(examId)
            .orElseThrow(() -> new DomainException("Examen introuvable."));

    JGroup group =
        groupRepository
            .findById(groupId)
            .orElseThrow(() -> new DomainException("Groupe introuvable."));

    boolean groupInExam = exam.getGroups().stream().anyMatch(g -> g.getId().equals(groupId));
    if (!groupInExam) {
      throw new DomainException("Ce groupe n'est pas concerné par cet examen.");
    }

    List<JStudent> students = studentRepository.findDetailedByCurrentGroupIdIn(List.of(groupId));
    Map<String, JGrade> gradesByStudent = new LinkedHashMap<>();
    List<String> studentIds = students.stream().map(JStudent::getId).toList();

    if (!studentIds.isEmpty()) {
      for (JGrade grade : gradeRepository.findByExamIdAndStudentIdIn(exam.getId(), studentIds)) {
        gradesByStudent.put(grade.getStudent().getId(), grade);
      }
    }

    List<GradeRow> rows = new ArrayList<>();
    for (JStudent student : students) {
      JGrade grade = gradesByStudent.get(student.getId());
      rows.add(
          new GradeRow(
              student.getId(),
              student.getStudentNumber(),
              fullName(student.getUser()),
              group.getId(),
              group.getRef(),
              grade == null ? null : grade.getId(),
              grade == null ? null : grade.getValue()));
    }

    return new GradeSheet(
        toExamOption(exam), exam.getCourse().getId(), exam.getCourse().getRef(), group, rows);
  }

  @Transactional
  public void saveGrade(String adminId, String examId, String studentId, BigDecimal value) {
    saveGrade(adminId, examId, studentId, value, null);
  }

  @Transactional
  public void saveGrade(
      String adminId, String examId, String studentId, BigDecimal value, String reason) {
    GradeContext context = loadGradeContext(examId, studentId, value);

    JGrade existing =
        gradeRepository.findByExamIdAndStudentId(context.exam.getId(), studentId).orElse(null);

    if (existing == null) {
      createNewGrade(context, value, reason, adminId);
    } else {
      updateExistingGrade(existing, context, value, reason, adminId);
    }
  }

  private void createNewGrade(
      GradeContext context, BigDecimal value, String reason, String adminId) {
    JUser admin =
        userRepository
            .findById(adminId)
            .orElseThrow(() -> new DomainException("Admin introuvable."));

    JGrade grade = new JGrade();
    grade.setId(UUID.randomUUID().toString());
    grade.setExam(context.exam);
    grade.setStudent(context.student);
    grade.setValue(value);
    grade.setGradedBy(context.teacher == null ? getDefaultTeacher() : context.teacher);
    grade.setGradedAt(Instant.now());

    JGrade saved = gradeRepository.save(grade);
    gradeHistoryService.logGradeChange(saved, null, value, resolveCreateReason(reason), admin);
  }

  private void updateExistingGrade(
      JGrade grade, GradeContext context, BigDecimal value, String reason, String adminId) {
    if (grade.getValue().compareTo(value) == 0) {
      throw new DomainException("La note n'a pas changé.");
    }
    if (reason == null || reason.isBlank()) {
      throw new DomainException("Un motif est obligatoire pour modifier une note.");
    }

    JUser admin =
        userRepository
            .findById(adminId)
            .orElseThrow(() -> new DomainException("Admin introuvable."));

    BigDecimal previous = grade.getValue();
    grade.setValue(value);
    grade.setGradedBy(context.teacher == null ? getDefaultTeacher() : context.teacher);
    grade.setGradedAt(Instant.now());

    JGrade saved = gradeRepository.save(grade);
    gradeHistoryService.logGradeChange(saved, previous, value, reason, admin);
  }

  private GradeContext loadGradeContext(String examId, String studentId, BigDecimal value) {
    JExam exam =
        examRepository
            .findDetailedById(examId)
            .orElseThrow(() -> new DomainException("Examen introuvable."));

    assertEditable(exam);

    BigDecimal maxGrade = maxGrade(exam.getCoefficient());
    if (value == null || value.compareTo(MIN_GRADE) < 0 || value.compareTo(maxGrade) > 0) {
      throw new DomainException("La note doit être comprise entre 0 et " + maxGrade + ".");
    }

    JStudent student =
        studentRepository
            .findDetailedById(studentId)
            .orElseThrow(() -> new DomainException("Étudiant introuvable."));

    if (student.getCurrentGroup() == null) {
      throw new DomainException("Cet étudiant n'appartient à aucun groupe.");
    }

    String studentGroupId = student.getCurrentGroup().getId();
    boolean examCoversGroup =
        exam.getGroups().stream().anyMatch(group -> group.getId().equals(studentGroupId));

    if (!examCoversGroup) {
      throw new DomainException("Cet étudiant n'est pas concerné par cet examen.");
    }

    JTeacher teacher = getDefaultTeacher();

    return new GradeContext(exam, student, teacher);
  }

  @Transactional(readOnly = true)
  public GradeHistoryView getHistory(String gradeId) {
    var histories = gradeHistoryRepository.findDetailedByGradeId(gradeId);
    if (histories.isEmpty()) {
      JGrade grade =
          gradeRepository
              .findDetailedById(gradeId)
              .orElseThrow(() -> new DomainException("Note introuvable."));
      return new GradeHistoryView(grade, List.of());
    }

    JGrade grade = histories.get(0).getGrade();
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

  private static void assertEditable(JExam exam) {
    if (exam.getSubmittedAt() != null) {
      throw new DomainException("Cet examen est déjà soumis.");
    }
  }

  private ExamOption toExamOption(JExam exam) {
    List<GroupOption> groups =
        exam.getGroups().stream()
            .sorted(Comparator.comparing(JGroup::getRef, String.CASE_INSENSITIVE_ORDER))
            .map(group -> new GroupOption(group.getId(), group.getRef()))
            .toList();

    List<String> groupIds = groups.stream().map(GroupOption::groupId).toList();
    int studentCount =
        groupIds.isEmpty()
            ? 0
            : (int) studentRepository.countByCurrentGroupIdInAndDeletedAtIsNull(groupIds);
    int gradedCount = (int) gradeRepository.countByExamId(exam.getId());
    var course = exam.getCourse();

    return new ExamOption(
        exam.getId(),
        exam.getDateExam(),
        exam.getCoefficient(),
        groups,
        course.getId(),
        course.getRef(),
        course.getTitle(),
        exam.getSubmittedAt(),
        gradedCount,
        studentCount);
  }

  private static BigDecimal maxGrade(BigDecimal coefficient) {
    return GRADE_SCALE.multiply(coefficient);
  }

  private static String resolveCreateReason(String reason) {
    return reason == null || reason.isBlank() ? "Saisie initiale par admin" : reason;
  }

  private static String fullName(JUser user) {
    if (user == null) {
      return "";
    }
    String first = user.getFirstName() == null ? "" : user.getFirstName();
    String last = user.getLastName() == null ? "" : user.getLastName();
    return (first + " " + last).trim();
  }

  private record GradeContext(JExam exam, JStudent student, JTeacher teacher) {}

  public record AdminHome(List<CourseOption> courses, int examCount, int gradeCount) {}

  public record CourseOption(String courseId, String ref, String title, Integer credits) {}

  public record GroupOption(String groupId, String ref) {}

  public record ExamOption(
      String examId,
      Instant dateExam,
      BigDecimal coefficient,
      List<GroupOption> groups,
      String courseId,
      String courseRef,
      String courseTitle,
      Instant submittedAt,
      int gradedCount,
      int studentCount) {
    public String getGroupRefs() {
      return groups.stream()
          .map(GroupOption::ref)
          .collect(java.util.stream.Collectors.joining(", "));
    }

    public BigDecimal getMaxGrade() {
      return maxGrade(coefficient);
    }

    public boolean isSubmitted() {
      return submittedAt != null;
    }
  }

  public record GradeRow(
      String studentId,
      String studentNumber,
      String fullName,
      String groupId,
      String groupRef,
      String gradeId,
      BigDecimal currentValue) {}

  public record GradeSheet(
      ExamOption exam, String courseId, String courseRef, JGroup group, List<GradeRow> rows) {}

  public record HistoryRow(
      Instant changedAt,
      String changedByName,
      BigDecimal previousValue,
      BigDecimal newValue,
      String reason) {}

  public record GradeHistoryView(JGrade grade, List<HistoryRow> entries) {}
}
