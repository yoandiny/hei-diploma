package mg.yoan.diploma.endpoint.rest.controller.student;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import lombok.AllArgsConstructor;
import mg.yoan.diploma.endpoint.event.EventProducer;
import mg.yoan.diploma.endpoint.event.model.SendTranscriptEmailRequested;
import mg.yoan.diploma.endpoint.rest.security.AuthenticatedUser;
import mg.yoan.diploma.service.TranscriptService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
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

  private static final String GRADES_PAGE = "/student/grades.html";

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
  public ResponseEntity<Void> emailOwnTranscript(
      @AuthenticationPrincipal AuthenticatedUser user, HttpServletRequest request) {
    if (transcriptService.getByStudentId(user.getUserId()).isEmpty()) {
      if (wantsHtml(request)) {
        return redirectToGrades("mailError=1");
      }
      return ResponseEntity.notFound().build();
    }
    var event = SendTranscriptEmailRequested.builder().studentId(user.getUserId()).build();
    eventProducer.accept(List.of(event));
    if (wantsHtml(request)) {
      return redirectToGrades("mailed=1");
    }
    return ResponseEntity.accepted().build();
  }

  private static boolean wantsHtml(HttpServletRequest request) {
    String accept = request.getHeader(HttpHeaders.ACCEPT);
    return accept != null && accept.contains("text/html");
  }

  private static ResponseEntity<Void> redirectToGrades(String query) {
    return ResponseEntity.status(HttpStatus.SEE_OTHER)
        .header(HttpHeaders.LOCATION, GRADES_PAGE + "?" + query)
        .build();
  }
}
