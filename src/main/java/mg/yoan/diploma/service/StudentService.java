package mg.yoan.diploma.service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import mg.yoan.diploma.domain.Role;
import mg.yoan.diploma.domain.Student;
import mg.yoan.diploma.repository.JGroupRepository;
import mg.yoan.diploma.repository.JPromotionRepository;
import mg.yoan.diploma.repository.JStudentGroupHistoryRepository;
import mg.yoan.diploma.repository.JStudentRepository;
import mg.yoan.diploma.repository.JUserRepository;
import mg.yoan.diploma.repository.mapper.StudentMapper;
import mg.yoan.diploma.repository.model.JGroup;
import mg.yoan.diploma.repository.model.JPromotion;
import mg.yoan.diploma.repository.model.JStudent;
import mg.yoan.diploma.repository.model.JStudentGroupHistory;
import mg.yoan.diploma.repository.model.JUser;
import org.springframework.dao.DataAccessException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
public class StudentService {

  private final JStudentRepository studentRepository;
  private final JUserRepository userRepository;
  private final JPromotionRepository promotionRepository;
  private final JGroupRepository groupRepository;
  private final JStudentGroupHistoryRepository groupHistoryRepository;
  private final PasswordEncoder passwordEncoder;

  @Transactional(readOnly = true)
  public List<Student> listAll() {
    return studentRepository.findAllDetailed().stream().map(StudentMapper::toDomain).toList();
  }

  @Transactional(readOnly = true)
  public Student getById(String id) {
    return studentRepository
        .findDetailedById(id)
        .map(StudentMapper::toDomain)
        .orElseThrow(() -> new DomainException("Étudiant introuvable."));
  }

  @Transactional
  public Student create(
      String firstName,
      String lastName,
      String email,
      String studentNumber,
      String promotionId,
      String groupId,
      String rawPassword,
      boolean enabled) {
    String normalizedEmail = normalizeEmail(email);
    String normalizedStudentNumber =
        required(studentNumber, "Le numéro d'étudiant est obligatoire.").trim();
    if (userRepository.findByEmail(normalizedEmail).isPresent()) {
      throw new DomainException("Cet email est déjà utilisé.");
    }
    if (studentRepository.existsByStudentNumber(normalizedStudentNumber)) {
      throw new DomainException("Ce numéro d'étudiant existe déjà.");
    }
    if (rawPassword == null || rawPassword.isBlank()) {
      throw new DomainException("Le mot de passe est obligatoire.");
    }

    JPromotion promotion =
        promotionRepository
            .findById(required(promotionId, "La promotion est obligatoire."))
            .orElseThrow(() -> new DomainException("Promotion introuvable."));
    JGroup group = resolveGroup(groupId, promotion);

    String id = UUID.randomUUID().toString();
    JUser user = new JUser();
    user.setId(id);
    user.setEmail(normalizedEmail);
    user.setFirstName(required(firstName, "Le prénom est obligatoire.").trim());
    user.setLastName(required(lastName, "Le nom est obligatoire.").trim());
    user.setPassword(passwordEncoder.encode(rawPassword));
    user.setRole(Role.STUDENT);
    user.setEnabled(enabled);
    user.setCreatedAt(Instant.now());

    JStudent student = new JStudent();
    student.setUser(user);
    student.setStudentNumber(normalizedStudentNumber);
    student.setPromotion(promotion);
    student.setCurrentGroup(group);

    try {
      JStudent saved = studentRepository.saveAndFlush(student);
      if (group != null) {
        JStudentGroupHistory history = new JStudentGroupHistory();
        history.setId(UUID.randomUUID().toString());
        history.setStudent(saved);
        history.setGroup(group);
        history.setStartDate(Instant.now());
        groupHistoryRepository.save(history);
      }
      return StudentMapper.toDomain(
          studentRepository.findDetailedById(saved.getId()).orElse(saved));
    } catch (DataAccessException exception) {
      throw new DomainException("Impossible d'enregistrer l'étudiant.", exception);
    }
  }

  @Transactional
  public Student changeGroup(String id, String groupId) {
    JStudent student = requireActive(id);
    JGroup group =
        resolveGroup(required(groupId, "Le groupe est obligatoire."), student.getPromotion());
    String currentGroupId =
        student.getCurrentGroup() == null ? null : student.getCurrentGroup().getId();
    if (group.getId().equals(currentGroupId)) {
      throw new DomainException("L'étudiant est déjà dans ce groupe.");
    }
    Instant now = Instant.now();
    groupHistoryRepository
        .findByStudentIdAndEndDateIsNull(student.getId())
        .ifPresent(
            history -> {
              history.setEndDate(now);
              groupHistoryRepository.save(history);
            });
    student.setCurrentGroup(group);
    studentRepository.save(student);
    JStudentGroupHistory history = new JStudentGroupHistory();
    history.setId(UUID.randomUUID().toString());
    history.setStudent(student);
    history.setGroup(group);
    history.setStartDate(now);
    groupHistoryRepository.save(history);
    return StudentMapper.toDomain(
        studentRepository.findDetailedById(student.getId()).orElse(student));
  }

  @Transactional
  public void suspend(String id) {
    JStudent student = requireActive(id);
    student.getUser().setEnabled(false);
    userRepository.save(student.getUser());
  }

  @Transactional
  public void unsuspend(String id) {
    JStudent student = requireActive(id);
    student.getUser().setEnabled(true);
    userRepository.save(student.getUser());
  }

  @Transactional
  public void softDelete(String id) {
    JStudent student = requireActive(id);
    student.setDeletedAt(Instant.now());
    student.getUser().setEnabled(false);
    userRepository.save(student.getUser());
    studentRepository.save(student);
  }

  private JStudent requireActive(String id) {
    return studentRepository
        .findDetailedById(id)
        .orElseThrow(() -> new DomainException("Étudiant introuvable."));
  }

  private JGroup resolveGroup(String groupId, JPromotion promotion) {
    if (groupId == null || groupId.isBlank()) {
      return null;
    }
    JGroup group =
        groupRepository
            .findDetailedById(groupId)
            .orElseThrow(() -> new DomainException("Groupe introuvable."));
    if (!group.getPromotion().getId().equals(promotion.getId())) {
      throw new DomainException("Le groupe doit appartenir à la promotion de l'étudiant.");
    }
    return group;
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
