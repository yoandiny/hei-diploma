package mg.yoan.diploma.service;

import java.util.List;
import lombok.AllArgsConstructor;
import mg.yoan.diploma.domain.Student;
import mg.yoan.diploma.repository.JStudentRepository;
import mg.yoan.diploma.repository.mapper.StudentMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
public class StudentService {

  private final JStudentRepository studentRepository;

  @Transactional(readOnly = true)
  public List<Student> listAll() {
    return studentRepository.findAllDetailed().stream().map(StudentMapper::toDomain).toList();
  }
}
