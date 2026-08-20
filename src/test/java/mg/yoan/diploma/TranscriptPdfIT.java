package mg.yoan.diploma;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import mg.yoan.diploma.domain.Course;
import mg.yoan.diploma.domain.Group;
import mg.yoan.diploma.domain.Promotion;
import mg.yoan.diploma.domain.Student;
import mg.yoan.diploma.domain.Teacher;
import mg.yoan.diploma.service.TeacherGradeService;
import mg.yoan.diploma.service.TranscriptPdfGenerator;
import mg.yoan.diploma.service.TranscriptService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class TranscriptPdfIT extends DiplomaIT {

  @Autowired private TeacherGradeService teacherGradeService;
  @Autowired private TranscriptService transcriptService;
  @Autowired private TranscriptPdfGenerator pdfGenerator;

  @Test
  void pdf_generator_builds_a_transcript_file() {
    Promotion promotion = newPromotion();
    Group group = newGroup(promotion);
    Course course = newCourse();
    Teacher teacher = newTeacher();
    Student student = newStudent(promotion, group);
    assign(course, teacher, group);
    var exam =
        teacherGradeService.createExam(
            teacher.getId().toString(),
            course.getId().toString(),
            Instant.parse("2026-06-01T08:00:00Z"),
            BigDecimal.ONE,
            List.of(group.getId().toString()));
    teacherGradeService.saveGrade(
        teacher.getId().toString(),
        exam.examId(),
        student.getId().toString(),
        new BigDecimal("14.00"));
    teacherGradeService.submitExam(teacher.getId().toString(), exam.examId());

    var transcript = transcriptService.getByStudentId(student.getId().toString()).orElseThrow();
    var pdf = pdfGenerator.generate(transcript);

    assertTrue(pdf.exists());
    assertTrue(pdf.length() > 0);
    assertTrue(pdf.getName().endsWith(".pdf"));
  }
}
