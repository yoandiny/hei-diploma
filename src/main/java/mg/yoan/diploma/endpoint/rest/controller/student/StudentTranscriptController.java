package mg.yoan.diploma.endpoint.rest.controller.student;

import java.util.List;
import lombok.AllArgsConstructor;
import mg.yoan.diploma.endpoint.event.EventProducer;
import mg.yoan.diploma.endpoint.event.model.SendTranscriptEmailRequested;
import mg.yoan.diploma.endpoint.rest.security.AuthenticatedUser;
import mg.yoan.diploma.service.TranscriptService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/students")
@AllArgsConstructor
public class StudentTranscriptController {

  private final TranscriptService transcriptService;
  private final EventProducer<SendTranscriptEmailRequested> eventProducer;

  @GetMapping("/me/transcript")
  @PreAuthorize("hasRole('STUDENT')")
  public ResponseEntity<TranscriptResponse> getOwnTranscript(
      @AuthenticationPrincipal AuthenticatedUser user) {
    return transcriptService
        .getByStudentId(user.getUserId())
        .map(TranscriptResponse::from)
        .map(ResponseEntity::ok)
        .orElseGet(() -> ResponseEntity.notFound().build());
  }

  @PostMapping("/me/transcript/email")
  @PreAuthorize("hasRole('STUDENT')")
  public ResponseEntity<Void> emailOwnTranscript(@AuthenticationPrincipal AuthenticatedUser user) {
    if (transcriptService.getByStudentId(user.getUserId()).isEmpty()) {
      return ResponseEntity.notFound().build();
    }
    var event = SendTranscriptEmailRequested.builder().studentId(user.getUserId()).build();
    eventProducer.accept(List.of(event));
    return ResponseEntity.accepted().build();
  }
}
