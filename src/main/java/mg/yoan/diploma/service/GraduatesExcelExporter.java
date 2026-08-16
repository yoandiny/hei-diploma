package mg.yoan.diploma.service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import mg.yoan.diploma.domain.Promotion;
import mg.yoan.diploma.domain.Student;
import mg.yoan.diploma.service.GraduationService.GraduatedStudent;
import org.springframework.stereotype.Component;

@Component
public class GraduatesExcelExporter {

  private static final String[] HEADERS = {
    "Référence", "Nom", "Prénom", "Email", "Groupe", "Promotion", "Moyenne générale"
  };

  public byte[] write(Promotion promotion, List<GraduatedStudent> graduates) {
    try (var output = new ByteArrayOutputStream();
        var zip = new ZipOutputStream(output, StandardCharsets.UTF_8)) {
      put(zip, "[Content_Types].xml", contentTypes());
      put(zip, "_rels/.rels", rootRels());
      put(zip, "xl/workbook.xml", workbook());
      put(zip, "xl/_rels/workbook.xml.rels", workbookRels());
      put(zip, "xl/worksheets/sheet1.xml", sheet(promotion, graduates));
      zip.finish();
      return output.toByteArray();
    } catch (IOException exception) {
      throw new UncheckedIOException(
          "Impossible de générer le fichier Excel des diplômés.", exception);
    }
  }

  private static String sheet(Promotion promotion, List<GraduatedStudent> graduates) {
    var rows = new ArrayList<String>();
    rows.add(rowXml(1, List.of(HEADERS)));
    int rowIndex = 2;
    for (GraduatedStudent graduate : graduates) {
      Student student = graduate.student();
      var user = student.getUser();
      var group = student.getCurrentGroup();
      rows.add(
          rowXml(
              rowIndex++,
              List.of(
                  nullToEmpty(student.getStudentNumber()),
                  user == null ? "" : nullToEmpty(user.getLastName()),
                  user == null ? "" : nullToEmpty(user.getFirstName()),
                  user == null ? "" : nullToEmpty(user.getEmail()),
                  group == null ? "" : group.getRef(),
                  promotion == null ? "" : nullToEmpty(promotion.getLabel()),
                  graduate.weightedAverage() == null
                      ? ""
                      : graduate.weightedAverage().toPlainString())));
    }
    return """
           <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
           <worksheet xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main">
             <sheetData>
           %s
             </sheetData>
           </worksheet>
           """
        .formatted(String.join("\n", rows));
  }

  private static String rowXml(int rowIndex, List<String> values) {
    var cells = new StringBuilder();
    for (int i = 0; i < values.size(); i++) {
      cells
          .append("      <c r=\"")
          .append(columnName(i))
          .append(rowIndex)
          .append("\" t=\"inlineStr\"><is><t xml:space=\"preserve\">")
          .append(escapeXml(values.get(i)))
          .append("</t></is></c>\n");
    }
    return "    <row r=\"%d\">%n%s    </row>".formatted(rowIndex, cells);
  }

  private static String columnName(int index) {
    return Character.toString('A' + index);
  }

  private static void put(ZipOutputStream zip, String name, String content) throws IOException {
    zip.putNextEntry(new ZipEntry(name));
    var writer = new OutputStreamWriter(zip, StandardCharsets.UTF_8);
    writer.write(content);
    writer.flush();
    zip.closeEntry();
  }

  private static String contentTypes() {
    return """
<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Types xmlns="http://schemas.openxmlformats.org/package/2006/content-types">
  <Default Extension="rels" ContentType="application/vnd.openxmlformats-package.relationships+xml"/>
  <Default Extension="xml" ContentType="application/xml"/>
  <Override PartName="/xl/workbook.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.sheet.main+xml"/>
  <Override PartName="/xl/worksheets/sheet1.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml"/>
</Types>
""";
  }

  private static String rootRels() {
    return """
<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
  <Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument" Target="xl/workbook.xml"/>
</Relationships>
""";
  }

  private static String workbook() {
    return """
<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<workbook xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main" xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships">
  <sheets>
    <sheet name="Diplomes" sheetId="1" r:id="rId1"/>
  </sheets>
</workbook>
""";
  }

  private static String workbookRels() {
    return """
<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
  <Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet" Target="worksheets/sheet1.xml"/>
</Relationships>
""";
  }

  private static String escapeXml(String value) {
    return value
        .replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")
        .replace("\"", "&quot;");
  }

  private static String nullToEmpty(String value) {
    return value == null ? "" : value;
  }
}
