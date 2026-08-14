package mg.yoan.diploma.repository;

import java.util.Optional;
import mg.yoan.diploma.repository.model.JTeacher;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JTeacherRepository extends JpaRepository<JTeacher, String> {

  Optional<JTeacher> findByEmployeeNumber(String employeeNumber);
}
