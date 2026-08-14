package mg.yoan.diploma.repository;

import java.util.Optional;
import mg.yoan.diploma.repository.model.JUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JUserRepository extends JpaRepository<JUser, String> {

  Optional<JUser> findByEmail(String email);
}
