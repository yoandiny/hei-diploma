package mg.yoan.diploma.service;

import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import mg.yoan.diploma.domain.Course;
import mg.yoan.diploma.repository.JCourseAssignmentRepository;
import mg.yoan.diploma.repository.JCourseRepository;
import mg.yoan.diploma.repository.JExamRepository;
import mg.yoan.diploma.repository.mapper.CourseMapper;
import mg.yoan.diploma.repository.model.JCourse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
public class CourseService {

  private final JCourseRepository courseRepository;
  private final JCourseAssignmentRepository assignmentRepository;
  private final JExamRepository examRepository;

  @Transactional(readOnly = true)
  public List<Course> listAll() {
    return courseRepository.findAllByOrderByRefAsc().stream().map(CourseMapper::toDomain).toList();
  }

  @Transactional(readOnly = true)
  public Course getById(String id) {
    return courseRepository
        .findById(id)
        .map(CourseMapper::toDomain)
        .orElseThrow(() -> new DomainException("Cours introuvable."));
  }

  @Transactional
  public Course save(String id, String ref, String title, Integer credits) {
    String normalizedRef = required(ref, "Le code du cours est obligatoire.").trim();
    String normalizedTitle = required(title, "L'intitulé du cours est obligatoire.").trim();
    if (credits == null || credits < 0) {
      throw new DomainException("Le nombre de crédits doit être positif ou nul.");
    }

    boolean duplicate =
        id == null || id.isBlank()
            ? courseRepository.existsByRefIgnoreCase(normalizedRef)
            : courseRepository.existsByRefIgnoreCaseAndIdNot(normalizedRef, id);
    if (duplicate) {
      throw new DomainException("Un cours avec ce code existe déjà.");
    }

    JCourse entity;
    if (id == null || id.isBlank()) {
      entity = new JCourse();
      entity.setId(UUID.randomUUID().toString());
    } else {
      entity =
          courseRepository
              .findById(id)
              .orElseThrow(() -> new DomainException("Cours introuvable."));
    }
    entity.setRef(normalizedRef);
    entity.setTitle(normalizedTitle);
    entity.setCredits(credits);
    return CourseMapper.toDomain(courseRepository.save(entity));
  }

  @Transactional
  public void delete(String id) {
    if (!courseRepository.existsById(id)) {
      throw new DomainException("Cours introuvable.");
    }
    if (assignmentRepository.existsByCourseId(id)) {
      throw new DomainException("Impossible de supprimer un cours encore affecté.");
    }
    if (!examRepository.findByCourseId(id).isEmpty()) {
      throw new DomainException("Impossible de supprimer un cours qui a déjà des examens.");
    }
    courseRepository.deleteById(id);
  }

  private static String required(String value, String message) {
    if (value == null || value.isBlank()) {
      throw new DomainException(message);
    }
    return value;
  }
}
