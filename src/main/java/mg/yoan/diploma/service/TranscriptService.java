package mg.yoan.diploma.service;

import java.util.Optional;
import java.util.UUID;
import lombok.AllArgsConstructor;
import mg.yoan.diploma.domain.Grade;
import mg.yoan.diploma.domain.Student;
import mg.yoan.diploma.domain.Transcript;
import mg.yoan.diploma.repository.JGradeRepository;
import mg.yoan.diploma.repository.JStudentRepository;
import mg.yoan.diploma.repository.mapper.ExamMapper;
import mg.yoan.diploma.repository.mapper.StudentMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
public class TranscriptService {

  private final JStudentRepository studentRepository;
  private final JGradeRepository gradeRepository;

  @Transactional(readOnly = true)
  public Optional<Transcript> getByStudentId(String studentId) {
    return studentRepository
        .findDetailedById(studentId)
        .map(
            entity -> {
              Student student = StudentMapper.toDomain(entity);
              var grades =
                  gradeRepository.findDetailedByStudentId(studentId).stream()
                      .filter(grade -> grade.getExam().getSubmittedAt() != null)
                      .map(
                          grade ->
                              Grade.builder()
                                  .id(UUID.fromString(grade.getId()))
                                  .exam(ExamMapper.toDomain(grade.getExam()))
                                  .student(student)
                                  .value(grade.getValue())
                                  .gradedAt(grade.getGradedAt())
                                  .build())
                      .toList();
              return Transcript.builder().student(student).grades(grades).build();
            });
  }
}
