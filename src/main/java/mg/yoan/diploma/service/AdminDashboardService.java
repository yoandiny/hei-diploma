package mg.yoan.diploma.service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import lombok.AllArgsConstructor;
import mg.yoan.diploma.repository.JCourseRepository;
import mg.yoan.diploma.repository.JGradeHistoryRepository;
import mg.yoan.diploma.repository.JStudentRepository;
import mg.yoan.diploma.repository.JTeacherRepository;
import mg.yoan.diploma.repository.model.JGradeHistory;
import mg.yoan.diploma.repository.model.JUser;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
public class AdminDashboardService {

  private static final int DASHBOARD_HISTORY_SIZE = 8;
  private static final int HISTORY_PAGE_SIZE = 200;

  private final JCourseRepository courseRepository;
  private final JTeacherRepository teacherRepository;
  private final JStudentRepository studentRepository;
  private final JGradeHistoryRepository gradeHistoryRepository;

  @Transactional(readOnly = true)
  public AdminHome loadHome() {
    List<GradeChange> recent =
        toRows(
            gradeHistoryRepository.findRecentDetailed(PageRequest.of(0, DASHBOARD_HISTORY_SIZE)));
    return new AdminHome(
        courseRepository.count(),
        studentRepository.countByDeletedAtIsNull(),
        teacherRepository.count(),
        gradeHistoryRepository.count(),
        recent);
  }

  @Transactional(readOnly = true)
  public List<GradeChange> listGradeChanges() {
    return toRows(gradeHistoryRepository.findRecentDetailed(PageRequest.of(0, HISTORY_PAGE_SIZE)));
  }

  private static List<GradeChange> toRows(List<JGradeHistory> histories) {
    return histories.stream()
        .map(
            history -> {
              var grade = history.getGrade();
              var student = grade.getStudent();
              var user = student.getUser();
              return new GradeChange(
                  history.getChangedAt(),
                  grade.getExam().getCourse().getRef(),
                  student.getStudentNumber(),
                  fullName(user),
                  history.getPreviousValue(),
                  history.getNewValue(),
                  fullName(history.getChangedBy()),
                  history.getReason());
            })
        .toList();
  }

  private static String fullName(JUser user) {
    if (user == null) {
      return "";
    }
    String first = user.getFirstName() == null ? "" : user.getFirstName();
    String last = user.getLastName() == null ? "" : user.getLastName();
    return (first + " " + last).trim();
  }

  public record AdminHome(
      long courseCount,
      long studentCount,
      long teacherCount,
      long gradeChangeCount,
      List<GradeChange> recentChanges) {}

  public record GradeChange(
      Instant changedAt,
      String courseRef,
      String studentNumber,
      String studentName,
      BigDecimal previousValue,
      BigDecimal newValue,
      String changedByName,
      String reason) {}
}
