package mg.yoan.diploma.endpoint.rest.controller.student;

import lombok.AllArgsConstructor;
import mg.yoan.diploma.endpoint.rest.security.AuthenticatedUser;
import mg.yoan.diploma.service.TranscriptService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/students")
@AllArgsConstructor
public class StudentTranscriptController {

  private final TranscriptService transcriptService;

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
}
