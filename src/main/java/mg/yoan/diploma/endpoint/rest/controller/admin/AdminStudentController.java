package mg.yoan.diploma.endpoint.rest.controller.admin;

import java.util.List;
import lombok.AllArgsConstructor;
import mg.yoan.diploma.service.StudentService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/students")
@AllArgsConstructor
public class AdminStudentController {

  private final StudentService studentService;

  @GetMapping
  @PreAuthorize("hasRole('ADMIN')")
  public List<StudentSummaryResponse> listStudents() {
    return studentService.listAll().stream().map(StudentSummaryResponse::from).toList();
  }
}
