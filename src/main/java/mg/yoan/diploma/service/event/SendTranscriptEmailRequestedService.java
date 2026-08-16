package mg.yoan.diploma.service.event;

import jakarta.mail.internet.InternetAddress;
import java.time.Duration;
import java.util.List;
import java.util.function.Consumer;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import mg.yoan.diploma.domain.Transcript;
import mg.yoan.diploma.endpoint.event.model.SendTranscriptEmailRequested;
import mg.yoan.diploma.file.bucket.BucketComponent;
import mg.yoan.diploma.mail.Email;
import mg.yoan.diploma.mail.Mailer;
import mg.yoan.diploma.service.TranscriptPdfGenerator;
import mg.yoan.diploma.service.TranscriptService;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class SendTranscriptEmailRequestedService implements Consumer<SendTranscriptEmailRequested> {

  private static final Duration LINK_VALIDITY = Duration.ofDays(7);

  private final TranscriptService transcriptService;
  private final TranscriptPdfGenerator pdfGenerator;
  private final BucketComponent bucketComponent;
  private final Mailer mailer;

  @SneakyThrows
  @Override
  public void accept(SendTranscriptEmailRequested event) {
    Transcript transcript =
        transcriptService
            .getByStudentId(event.getStudentId())
            .orElseThrow(
                () -> new IllegalStateException("Student not found: " + event.getStudentId()));

    var user = transcript.getStudent().getUser();
    if (user == null || user.getEmail() == null) {
      throw new IllegalStateException("No email on file for student: " + event.getStudentId());
    }

    var pdfFile = pdfGenerator.generate(transcript);
    var bucketKey = "transcripts/" + event.getStudentId() + "/" + pdfFile.getName();
    bucketComponent.upload(pdfFile, bucketKey);
    var downloadUri = bucketComponent.presign(bucketKey, LINK_VALIDITY);

    mailer.accept(
        new Email(
            new InternetAddress(user.getEmail()),
            List.of(),
            List.of(),
            "Votre relevé de notes",
            "Bonjour "
                + user.getFullName()
                + ",<br/><br/>Votre relevé de notes est disponible ici : "
                + "<a href=\""
                + downloadUri
                + "\">Télécharger mon relevé</a><br/><br/>"
                + "Ce lien est valable 7 jours.",
            List.of()));
  }
}
