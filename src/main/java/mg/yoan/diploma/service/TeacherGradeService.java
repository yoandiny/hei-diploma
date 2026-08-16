package mg.yoan.diploma.service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import mg.yoan.diploma.domain.CourseAssignment;
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
import mg.yoan.diploma.repository.model.JCourse;
import mg.yoan.diploma.repository.model.JExam;
import mg.yoan.diploma.repository.model.JGrade;
import mg.yoan.diploma.repository.model.JGradeHistory;
import mg.yoan.diploma.repository.model.JGroup;
import mg.yoan.diploma.repository.model.JStudent;
import mg.yoan.diploma.repository.model.JUser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
public class TeacherGradeService {

  private static final BigDecimal MIN_GRADE = BigDecimal.ZERO;
  private static final BigDecimal GRADE_SCALE = new BigDecimal("20");

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
  public List<ExamOption> listExams(String teacherId, String courseId) {
    List<AssignedGroup> assigned = assignedGroups(teacherId, courseId);
    Set<String> assignedIds =
        assigned.stream()
            .map(AssignedGroup::groupId)
            .collect(Collectors.toCollection(HashSet::new));
    return examRepository.findDetailedByCourseId(courseId).stream()
        .filter(
            exam -> {
              var groups = exam.getGroups();
              return groups != null
                  && groups.stream().anyMatch(group -> assignedIds.contains(group.getId()));
            })
        .sorted(Comparator.comparing(JExam::getDateExam).reversed())
        .map(this::toExamOption)
        .toList();
  }

  @Transactional(readOnly = true)
  public List<ExamOption> listTeacherExams(String teacherId) {
    return listAssignedCourses(teacherId).stream()
        .flatMap(course -> listExams(teacherId, course.courseId()).stream())
        .sorted(Comparator.comparing(ExamOption::dateExam).reversed())
        .toList();
  }

  @Transactional(readOnly = true)
  public TeacherHome loadHome(String teacherId) {
    List<AssignedCourse> courses = listAssignedCourses(teacherId);
    List<ExamOption> exams =
        courses.stream()
            .flatMap(course -> listExams(teacherId, course.courseId()).stream())
            .sorted(Comparator.comparing(ExamOption::dateExam).reversed())
            .toList();
    List<ExamOption> drafts = exams.stream().filter(exam -> !exam.isSubmitted()).toList();
    int groupCount =
        (int)
            courses.stream()
                .flatMap(course -> course.groups().stream())
                .map(AssignedGroup::groupId)
                .distinct()
                .count();
    int studentCount =
        courses.stream()
            .flatMap(course -> course.groups().stream())
            .collect(
                Collectors.toMap(
                    AssignedGroup::groupId, AssignedGroup::studentCount, (left, right) -> left))
            .values()
            .stream()
            .mapToInt(Integer::intValue)
            .sum();
    return new TeacherHome(courses, exams, drafts, groupCount, studentCount);
  }

  @Transactional(readOnly = true)
  public ExamOption getExam(String teacherId, String examId) {
    return toExamOption(loadAccessibleExam(teacherId, examId));
  }

  @Transactional
  public void submitExam(String teacherId, String examId) {
    JExam exam = loadAccessibleExam(teacherId, examId);
    assertEditable(exam);
    ExamOption option = toExamOption(exam);
    if (option.studentCount() > 0 && option.gradedCount() < option.studentCount()) {
      throw new DomainException("Saisissez toutes les notes avant de soumettre l'examen.");
    }
    exam.setSubmittedAt(Instant.now());
    examRepository.save(exam);
  }

  @Transactional
  public ExamOption createExam(
      String teacherId,
      String courseId,
      Instant dateExam,
      BigDecimal coefficient,
      List<String> groupIds) {
    List<AssignedGroup> assigned = assignedGroups(teacherId, courseId);
    if (dateExam == null) {
      throw new DomainException("La date de l'examen est obligatoire.");
    }
    if (coefficient == null || coefficient.compareTo(BigDecimal.ZERO) <= 0) {
      throw new DomainException("Le coefficient doit être supérieur à 0.");
    }
    if (groupIds == null || groupIds.isEmpty()) {
      throw new DomainException("Choisissez au moins une classe pour cet examen.");
    }
    Set<String> assignedIds =
        assigned.stream()
            .map(AssignedGroup::groupId)
            .collect(Collectors.toCollection(HashSet::new));
    Set<String> uniqueGroupIds = new LinkedHashSet<>();
    for (String groupId : groupIds) {
      if (groupId == null || groupId.isBlank()) {
        continue;
      }
      if (!assignedIds.contains(groupId)) {
        throw new DomainException("Vous n'êtes pas responsable de ce cours pour ce groupe.");
      }
      uniqueGroupIds.add(groupId);
    }
    if (uniqueGroupIds.isEmpty()) {
      throw new DomainException("Choisissez au moins une classe pour cet examen.");
    }
    JCourse course =
        courseRepository
            .findById(courseId)
            .orElseThrow(() -> new DomainException("Cours introuvable."));
    List<JGroup> groups = groupRepository.findAllById(uniqueGroupIds);
    if (groups.size() != uniqueGroupIds.size()) {
      throw new DomainException("Classe introuvable.");
    }
    JExam exam = new JExam();
    exam.setId(UUID.randomUUID().toString());
    exam.setCourse(course);
    exam.setDateExam(dateExam);
    exam.setCoefficient(coefficient);
    exam.setGroups(new LinkedHashSet<>(groups));
    return toExamOption(examRepository.save(exam));
  }

  @Transactional(readOnly = true)
  public GradeSheet getGradeSheet(String teacherId, String examId, String groupId) {
    JExam exam = loadAccessibleExam(teacherId, examId);
    String courseId = exam.getCourse().getId();
    List<AssignedGroup> assignedGroups = assignedGroups(teacherId, courseId);
    Set<String> examGroupIds =
        exam.getGroups().stream().map(JGroup::getId).collect(Collectors.toCollection(HashSet::new));
    List<AssignedGroup> examGroups =
        assignedGroups.stream().filter(group -> examGroupIds.contains(group.groupId())).toList();
    if (examGroups.isEmpty()) {
      throw new DomainException("Cet examen ne concerne aucune de vos classes.");
    }
    List<AssignedGroup> selectedGroups = examGroups;
    if (groupId != null && !groupId.isBlank()) {
      selectedGroups =
          examGroups.stream().filter(group -> group.groupId().equals(groupId)).toList();
      if (selectedGroups.isEmpty()) {
        throw new DomainException("Cette classe n'est pas concernée par cet examen.");
      }
    }
    List<String> groupIds = selectedGroups.stream().map(AssignedGroup::groupId).toList();
    List<JStudent> students = studentRepository.findDetailedByCurrentGroupIdIn(groupIds);
    List<String> studentIds = students.stream().map(JStudent::getId).toList();
    Map<String, JGrade> gradesByStudent = new LinkedHashMap<>();
    if (!studentIds.isEmpty()) {
      for (JGrade grade : gradeRepository.findByExamIdAndStudentIdIn(exam.getId(), studentIds)) {
        gradesByStudent.put(grade.getStudent().getId(), grade);
      }
    }
    List<GradeRow> rows = new ArrayList<>();
    for (JStudent student : students) {
      JGrade grade = gradesByStudent.get(student.getId());
      var group = student.getCurrentGroup();
      rows.add(
          new GradeRow(
              student.getId(),
              student.getStudentNumber(),
              fullName(student.getUser()),
              group == null ? null : group.getId(),
              group == null ? "" : group.getRef(),
              grade == null ? null : grade.getId(),
              grade == null ? null : grade.getValue()));
    }
    return new GradeSheet(
        toExamOption(exam),
        courseId,
        exam.getCourse().getRef(),
        assignedGroups,
        examGroups,
        selectedGroups,
        rows);
  }

  @Transactional
  public void saveGrade(String teacherId, String examId, String studentId, BigDecimal value) {
    JExam exam = loadAccessibleExam(teacherId, examId);
    String courseId = exam.getCourse().getId();
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
    assertResponsible(teacherId, courseId, studentGroupId);
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
            .findByExamIdAndStudentId(exam.getId(), studentId)
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
    history.setReason(previous == null ? "Saisie initiale" : "Modification de note");
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
      assertCanViewGrade(teacherId, grade);
      return new GradeHistoryView(grade, List.of());
    }
    JGrade grade = histories.get(0).getGrade();
    assertCanViewGrade(teacherId, grade);
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

  private void assertCanViewGrade(String teacherId, JGrade grade) {
    String groupId =
        grade.getStudent().getCurrentGroup() == null
            ? null
            : grade.getStudent().getCurrentGroup().getId();
    if (groupId == null
        || !assignmentRepository.existsByCourseIdAndGroupIdAndTeacherId(
            grade.getExam().getCourse().getId(), groupId, teacherId)) {
      throw new DomainException("Vous n'êtes pas responsable de cette note.");
    }
  }

  private List<AssignedGroup> assignedGroups(String teacherId, String courseId) {
    assertTeachesCourse(teacherId, courseId);
    List<AssignedGroup> groups = new ArrayList<>();
    for (CourseAssignment assignment :
        assignmentRepository.findDetailedByTeacherId(teacherId).stream()
            .map(CourseAssignmentMapper::toDomain)
            .toList()) {
      if (!assignment.getCourse().getId().toString().equals(courseId)) {
        continue;
      }
      var group = assignment.getGroup();
      int students = (int) studentRepository.countByCurrentGroupId(group.getId().toString());
      groups.add(new AssignedGroup(group.getId().toString(), group.getRef(), students));
    }
    return groups;
  }

  private JExam loadAccessibleExam(String teacherId, String examId) {
    JExam exam =
        examRepository
            .findDetailedById(examId)
            .orElseThrow(() -> new DomainException("Examen introuvable."));
    Set<String> assignedIds =
        assignedGroups(teacherId, exam.getCourse().getId()).stream()
            .map(AssignedGroup::groupId)
            .collect(Collectors.toCollection(HashSet::new));
    boolean visible =
        exam.getGroups().stream().anyMatch(group -> assignedIds.contains(group.getId()));
    if (!visible) {
      throw new DomainException("Cet examen ne concerne aucune de vos classes.");
    }
    return exam;
  }

  private static void assertEditable(JExam exam) {
    if (exam.getSubmittedAt() != null) {
      throw new DomainException("Cet examen est déjà soumis.");
    }
  }

  private ExamOption toExamOption(JExam exam) {
    List<AssignedGroup> groups =
        exam.getGroups().stream()
            .sorted(Comparator.comparing(JGroup::getRef, String.CASE_INSENSITIVE_ORDER))
            .map(group -> new AssignedGroup(group.getId(), group.getRef(), 0))
            .toList();
    List<String> groupIds = groups.stream().map(AssignedGroup::groupId).toList();
    int studentCount =
        groupIds.isEmpty() ? 0 : (int) studentRepository.countByCurrentGroupIdIn(groupIds);
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

  public record TeacherHome(
      List<AssignedCourse> courses,
      List<ExamOption> exams,
      List<ExamOption> draftExams,
      int groupCount,
      int studentCount) {}

  public record ExamOption(
      String examId,
      Instant dateExam,
      BigDecimal coefficient,
      List<AssignedGroup> groups,
      String courseId,
      String courseRef,
      String courseTitle,
      Instant submittedAt,
      int gradedCount,
      int studentCount) {
    public String getGroupRefs() {
      return groups.stream().map(AssignedGroup::ref).collect(Collectors.joining(", "));
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
      ExamOption exam,
      String courseId,
      String courseRef,
      List<AssignedGroup> allGroups,
      List<AssignedGroup> examGroups,
      List<AssignedGroup> selectedGroups,
      List<GradeRow> rows) {}

  public record HistoryRow(
      Instant changedAt,
      String changedByName,
      BigDecimal previousValue,
      BigDecimal newValue,
      String reason) {}

  public record GradeHistoryView(JGrade grade, List<HistoryRow> entries) {}
}
