package mg.yoan.diploma.service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import mg.yoan.diploma.domain.Role;
import mg.yoan.diploma.domain.Teacher;
import mg.yoan.diploma.repository.JCourseAssignmentRepository;
import mg.yoan.diploma.repository.JTeacherRepository;
import mg.yoan.diploma.repository.JUserRepository;
import mg.yoan.diploma.repository.mapper.TeacherMapper;
import mg.yoan.diploma.repository.model.JTeacher;
import mg.yoan.diploma.repository.model.JUser;
import org.springframework.dao.DataAccessException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
public class TeacherService {

  private final JTeacherRepository teacherRepository;
  private final JUserRepository userRepository;
  private final JCourseAssignmentRepository assignmentRepository;
  private final PasswordEncoder passwordEncoder;

  @Transactional(readOnly = true)
  public List<Teacher> listAll() {
    return teacherRepository.findAllDetailed().stream().map(TeacherMapper::toDomain).toList();
  }

  @Transactional(readOnly = true)
  public Teacher getById(String id) {
    return teacherRepository
        .findDetailedById(id)
        .map(TeacherMapper::toDomain)
        .orElseThrow(() -> new DomainException("Enseignant introuvable."));
  }

  @Transactional
  public Teacher create(
      String firstName,
      String lastName,
      String email,
      String employeeNumber,
      String rawPassword,
      boolean enabled) {
    String normalizedEmail = normalizeEmail(email);
    String normalizedEmployee =
        required(employeeNumber, "Le numéro d'employé est obligatoire.").trim();
    if (userRepository.findByEmail(normalizedEmail).isPresent()) {
      throw new DomainException("Cet email est déjà utilisé.");
    }
    if (teacherRepository.existsByEmployeeNumber(normalizedEmployee)) {
      throw new DomainException("Ce numéro d'employé existe déjà.");
    }
    if (rawPassword == null || rawPassword.isBlank()) {
      throw new DomainException("Le mot de passe est obligatoire.");
    }

    String id = UUID.randomUUID().toString();
    JUser user = new JUser();
    user.setId(id);
    user.setEmail(normalizedEmail);
    user.setFirstName(required(firstName, "Le prénom est obligatoire.").trim());
    user.setLastName(required(lastName, "Le nom est obligatoire.").trim());
    user.setPassword(passwordEncoder.encode(rawPassword));
    user.setRole(Role.TEACHER);
    user.setEnabled(enabled);
    user.setCreatedAt(Instant.now());

    JTeacher teacher = new JTeacher();
    teacher.setUser(user);
    teacher.setEmployeeNumber(normalizedEmployee);
    try {
      JTeacher saved = teacherRepository.saveAndFlush(teacher);
      return TeacherMapper.toDomain(
          teacherRepository.findDetailedById(saved.getId()).orElse(saved));
    } catch (DataAccessException exception) {
      throw new DomainException("Impossible d'enregistrer l'enseignant.", exception);
    }
  }

  @Transactional
  public Teacher update(
      String id,
      String firstName,
      String lastName,
      String email,
      String employeeNumber,
      String rawPassword,
      boolean enabled) {
    JTeacher teacher =
        teacherRepository
            .findDetailedById(id)
            .orElseThrow(() -> new DomainException("Enseignant introuvable."));
    JUser user = teacher.getUser();

    String normalizedEmail = normalizeEmail(email);
    String normalizedEmployee =
        required(employeeNumber, "Le numéro d'employé est obligatoire.").trim();

    userRepository
        .findByEmail(normalizedEmail)
        .filter(existing -> !existing.getId().equals(id))
        .ifPresent(
            ignored -> {
              throw new DomainException("Cet email est déjà utilisé.");
            });
    if (teacherRepository.existsByEmployeeNumberAndIdNot(normalizedEmployee, id)) {
      throw new DomainException("Ce numéro d'employé existe déjà.");
    }

    user.setEmail(normalizedEmail);
    user.setFirstName(required(firstName, "Le prénom est obligatoire.").trim());
    user.setLastName(required(lastName, "Le nom est obligatoire.").trim());
    user.setEnabled(enabled);
    if (rawPassword != null && !rawPassword.isBlank()) {
      user.setPassword(passwordEncoder.encode(rawPassword));
    }
    teacher.setEmployeeNumber(normalizedEmployee);
    userRepository.save(user);
    return TeacherMapper.toDomain(teacherRepository.save(teacher));
  }

  @Transactional
  public void delete(String id) {
    JTeacher teacher =
        teacherRepository
            .findById(id)
            .orElseThrow(() -> new DomainException("Enseignant introuvable."));
    if (assignmentRepository.existsByTeacherId(id)) {
      throw new DomainException("Impossible de supprimer un enseignant encore affecté à un cours.");
    }
    teacherRepository.delete(teacher);
    userRepository.deleteById(id);
  }

  private static String normalizeEmail(String email) {
    return required(email, "L'email est obligatoire.").trim().toLowerCase();
  }

  private static String required(String value, String message) {
    if (value == null || value.isBlank()) {
      throw new DomainException(message);
    }
    return value;
  }
}
