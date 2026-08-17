package mg.yoan.diploma.endpoint.rest.controller.admin;

import java.util.List;
import lombok.AllArgsConstructor;
import mg.yoan.diploma.endpoint.event.EventProducer;
import mg.yoan.diploma.endpoint.event.model.VerifyStudentEmailRequested;
import mg.yoan.diploma.service.StudentService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/students")
@AllArgsConstructor
public class AdminStudentController {

  private final StudentService studentService;
  private final EventProducer<VerifyStudentEmailRequested> verifyEmailEventProducer;

  @GetMapping
  @PreAuthorize("hasRole('ADMIN')")
  public List<StudentSummaryResponse> listStudents() {
    return studentService.listAll().stream().map(StudentSummaryResponse::from).toList();
  }

  @PostMapping("/verify-emails")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<Void> verifyAllStudentEmails() {
    var events =
        studentService.listAll().stream()
            .map(
                student ->
                    VerifyStudentEmailRequested.builder()
                        .studentId(student.getId().toString())
                        .build())
            .toList();

    if (!events.isEmpty()) {
      verifyEmailEventProducer.accept(events);
    }

    return ResponseEntity.accepted().build();
  }
}
