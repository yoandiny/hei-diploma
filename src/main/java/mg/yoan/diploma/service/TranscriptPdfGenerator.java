package mg.yoan.diploma.service;

import static java.io.File.createTempFile;

import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import lombok.SneakyThrows;
import mg.yoan.diploma.domain.Transcript;
import org.springframework.stereotype.Service;

@Service
public class TranscriptPdfGenerator {

  private static final DateTimeFormatter DATE_FORMAT =
      DateTimeFormatter.ofPattern("dd/MM/yyyy").withZone(ZoneId.of("Indian/Antananarivo"));

  @SneakyThrows
  public File generate(Transcript transcript) {
    var student = transcript.getStudent();
    var user = student.getUser();
    var promotion = student.getPromotion();

    File file = createTempFile("transcript-" + student.getId(), ".pdf");
    Document document = new Document();
    PdfWriter.getInstance(document, new FileOutputStream(file));
    document.open();

    Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16);
    Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 11);
    Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11);

    Paragraph title = new Paragraph("Relevé de notes", titleFont);
    title.setAlignment(Element.ALIGN_CENTER);
    document.add(title);
    document.add(new Paragraph(" "));

    document.add(
        new Paragraph(
            (user == null ? "" : user.getFullName()) + " - " + student.getStudentNumber(),
            normalFont));
    if (promotion != null) {
      document.add(
          new Paragraph(
              "Promotion "
                  + promotion.getLabel()
                  + " ("
                  + promotion.getStartYear()
                  + " - "
                  + promotion.getEndYear()
                  + ")",
              normalFont));
    }
    document.add(new Paragraph(" "));

    PdfPTable table = new PdfPTable(5);
    table.setWidthPercentage(100);
    addHeaderCell(table, "Cours", headerFont);
    addHeaderCell(table, "Crédits", headerFont);
    addHeaderCell(table, "Date examen", headerFont);
    addHeaderCell(table, "Coefficient", headerFont);
    addHeaderCell(table, "Note", headerFont);

    for (var grade : transcript.getGrades()) {
      var course = grade.getExam().getCourse();
      table.addCell(new PdfPCell(new Paragraph(course.getTitle(), normalFont)));
      table.addCell(new PdfPCell(new Paragraph(String.valueOf(course.getCredits()), normalFont)));
      table.addCell(
          new PdfPCell(
              new Paragraph(DATE_FORMAT.format(grade.getExam().getDateExam()), normalFont)));
      table.addCell(
          new PdfPCell(new Paragraph(grade.getExam().getCoefficient().toString(), normalFont)));
      table.addCell(new PdfPCell(new Paragraph(grade.getValue().toString(), normalFont)));
    }
    document.add(table);
    document.add(new Paragraph(" "));

    var average = transcript.weightedAverage();
    document.add(
        new Paragraph(
            "Moyenne générale : " + (average == null ? "N/A" : average.toString()), headerFont));

    document.close();
    return file;
  }

  private void addHeaderCell(PdfPTable table, String text, Font font) {
    var cell = new PdfPCell(new Paragraph(text, font));
    cell.setBackgroundColor(new java.awt.Color(230, 230, 230));
    table.addCell(cell);
  }
}
