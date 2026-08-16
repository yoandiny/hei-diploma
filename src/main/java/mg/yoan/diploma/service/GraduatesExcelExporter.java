package mg.yoan.diploma.service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.List;
import mg.yoan.diploma.domain.Promotion;
import mg.yoan.diploma.domain.Student;
import mg.yoan.diploma.service.GraduationService.GraduatedStudent;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;

@Component
public class GraduatesExcelExporter {

  public byte[] write(Promotion promotion, List<GraduatedStudent> graduates) {
    try (var workbook = new XSSFWorkbook();
        var output = new ByteArrayOutputStream()) {
      Sheet sheet = workbook.createSheet("Diplômés");
      Row header = sheet.createRow(0);
      header.createCell(0).setCellValue("Référence");
      header.createCell(1).setCellValue("Nom");
      header.createCell(2).setCellValue("Prénom");
      header.createCell(3).setCellValue("Email");
      header.createCell(4).setCellValue("Groupe");
      header.createCell(5).setCellValue("Promotion");
      header.createCell(6).setCellValue("Moyenne générale");

      int rowIndex = 1;
      for (GraduatedStudent graduate : graduates) {
        Student student = graduate.student();
        var user = student.getUser();
        var group = student.getCurrentGroup();
        Row row = sheet.createRow(rowIndex++);
        row.createCell(0).setCellValue(student.getStudentNumber());
        row.createCell(1).setCellValue(user == null ? "" : nullToEmpty(user.getLastName()));
        row.createCell(2).setCellValue(user == null ? "" : nullToEmpty(user.getFirstName()));
        row.createCell(3).setCellValue(user == null ? "" : nullToEmpty(user.getEmail()));
        row.createCell(4).setCellValue(group == null ? "" : group.getRef());
        row.createCell(5).setCellValue(promotion == null ? "" : promotion.getLabel());
        if (graduate.weightedAverage() == null) {
          row.createCell(6).setCellValue("");
        } else {
          row.createCell(6).setCellValue(graduate.weightedAverage().doubleValue());
        }
      }

      for (int column = 0; column <= 6; column++) {
        sheet.autoSizeColumn(column);
      }
      workbook.write(output);
      return output.toByteArray();
    } catch (IOException exception) {
      throw new UncheckedIOException(
          "Impossible de générer le fichier Excel des diplômés.", exception);
    }
  }

  private static String nullToEmpty(String value) {
    return value == null ? "" : value;
  }
}
