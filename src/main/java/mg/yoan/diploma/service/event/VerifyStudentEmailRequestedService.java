package mg.yoan.diploma.service.event;

import jakarta.mail.internet.InternetAddress;
import java.util.function.Consumer;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mg.yoan.diploma.endpoint.event.model.VerifyStudentEmailRequested;
import mg.yoan.diploma.mail.EmailAddressVerifier;
import mg.yoan.diploma.repository.JStudentRepository;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
@Slf4j
public class VerifyStudentEmailRequestedService implements Consumer<VerifyStudentEmailRequested> {

  private final JStudentRepository studentRepository;
  private final EmailAddressVerifier emailAddressVerifier;

  @Override
  public void accept(VerifyStudentEmailRequested event) {
    var student =
        studentRepository
            .findDetailedById(event.getStudentId())
            .orElseThrow(
                () -> new IllegalStateException("Student not found: " + event.getStudentId()));

    var email = student.getUser().getEmail();
    try {
      emailAddressVerifier.accept(new InternetAddress(email));
    } catch (Exception e) {
      log.error("SES verification request failed for {}: {}", email, e.getMessage());
      throw new RuntimeException(e);
    }
  }
}
