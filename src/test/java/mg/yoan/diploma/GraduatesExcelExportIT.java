package mg.yoan.diploma;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import mg.yoan.diploma.domain.Course;
import mg.yoan.diploma.domain.Group;
import mg.yoan.diploma.domain.Promotion;
import mg.yoan.diploma.domain.Student;
import mg.yoan.diploma.domain.Teacher;
import mg.yoan.diploma.service.GraduatesExcelExporter;
import mg.yoan.diploma.service.GraduationService;
import mg.yoan.diploma.service.TeacherGradeService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class GraduatesExcelExportIT extends DiplomaIT {

  @Autowired private TeacherGradeService teacherGradeService;
  @Autowired private GraduationService graduationService;
  @Autowired private GraduatesExcelExporter excelExporter;

  @Test
  void export_contains_every_graduate_with_their_average() throws Exception {
    Promotion promotion = newPromotion();
    Group group = newGroup(promotion);
    Course course = newCourse();
    Teacher teacher = newTeacher();
    Student graduate = newStudent(promotion, group);
    Student notGraduating = newStudent(promotion, group);
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
        graduate.getId().toString(),
        new BigDecimal("15.00"));
    teacherGradeService.saveGrade(
        teacher.getId().toString(),
        exam.examId(),
        notGraduating.getId().toString(),
        new BigDecimal("6.00"));

    var graduates = graduationService.listGraduates(promotion.getId().toString());
    assertEquals(1, graduates.size());

    byte[] excelBytes = excelExporter.write(promotion, graduates);
    String sheetXml = extractSheetXml(excelBytes);

    assertTrue(sheetXml.contains(graduate.getStudentNumber()));
    assertTrue(sheetXml.contains("15.00") || sheetXml.contains("15"));
    assertTrue(!sheetXml.contains(notGraduating.getStudentNumber()));
  }

  @Test
  void export_of_empty_graduate_list_is_still_a_valid_workbook() throws Exception {
    Promotion promotion = newPromotion();

    byte[] excelBytes = excelExporter.write(promotion, List.of());
    String sheetXml = extractSheetXml(excelBytes);

    assertTrue(sheetXml.contains("Référence"));
  }

  private static String extractSheetXml(byte[] excelBytes) throws Exception {
    try (var zip =
        new ZipInputStream(new ByteArrayInputStream(excelBytes), StandardCharsets.UTF_8)) {
      ZipEntry entry;
      while ((entry = zip.getNextEntry()) != null) {
        if (entry.getName().equals("xl/worksheets/sheet1.xml")) {
          return new String(zip.readAllBytes(), StandardCharsets.UTF_8);
        }
      }
    }
    throw new AssertionError("sheet1.xml entry not found in generated workbook");
  }
}
