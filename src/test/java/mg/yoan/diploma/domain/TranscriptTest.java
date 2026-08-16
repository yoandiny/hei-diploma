package mg.yoan.diploma.domain;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class TranscriptTest {

  private static final UUID COURSE_L1 = UUID.fromString("11111111-1111-1111-1111-111111111111");
  private static final UUID COURSE_L2 = UUID.fromString("22222222-2222-2222-2222-222222222222");
  private static final UUID COURSE_L3 = UUID.fromString("33333333-3333-3333-3333-333333333333");

  @Test
  void is_graduated_when_every_course_and_overall_average_are_at_least_ten() {
    Transcript transcript =
        transcript(grade(COURSE_L1, "12.00"), grade(COURSE_L2, "10.00"), grade(COURSE_L3, "11.50"));

    assertTrue(transcript.isGraduated(Set.of(COURSE_L1, COURSE_L2, COURSE_L3)));
  }

  @Test
  void is_not_graduated_when_one_course_is_below_ten() {
    Transcript transcript =
        transcript(grade(COURSE_L1, "12.00"), grade(COURSE_L2, "9.99"), grade(COURSE_L3, "15.00"));

    assertFalse(transcript.isGraduated(Set.of(COURSE_L1, COURSE_L2, COURSE_L3)));
  }

  @Test
  void is_not_graduated_when_a_required_course_has_no_grade() {
    Transcript transcript = transcript(grade(COURSE_L1, "14.00"), grade(COURSE_L2, "14.00"));

    assertFalse(transcript.isGraduated(Set.of(COURSE_L1, COURSE_L2, COURSE_L3)));
  }

  @Test
  void is_not_graduated_when_overall_average_is_below_ten() {
    UUID extraCourse = UUID.fromString("44444444-4444-4444-4444-444444444444");
    Transcript transcript =
        transcript(
            grade(COURSE_L1, "12.00"),
            grade(COURSE_L2, "12.00"),
            grade(COURSE_L3, "12.00"),
            grade(extraCourse, "0.00", "20"));

    assertFalse(transcript.isGraduated(Set.of(COURSE_L1, COURSE_L2, COURSE_L3)));
  }

  @Test
  void is_not_graduated_without_required_courses() {
    Transcript transcript = transcript(grade(COURSE_L1, "20.00"));

    assertFalse(transcript.isGraduated(Set.of()));
  }

  private static Transcript transcript(Grade... grades) {
    return Transcript.builder().grades(List.of(grades)).build();
  }

  private static Grade grade(UUID courseId, String value) {
    return grade(courseId, value, "1");
  }

  private static Grade grade(UUID courseId, String value, String coefficient) {
    Course course = Course.builder().id(courseId).ref("C").title("Cours").credits(6).build();
    Exam exam =
        Exam.builder()
            .id(UUID.randomUUID())
            .course(course)
            .dateExam(Instant.parse("2024-06-01T08:00:00Z"))
            .coefficient(new BigDecimal(coefficient))
            .build();
    return Grade.builder()
        .id(UUID.randomUUID())
        .exam(exam)
        .value(new BigDecimal(value))
        .gradedAt(Instant.parse("2024-06-02T08:00:00Z"))
        .build();
  }
}
