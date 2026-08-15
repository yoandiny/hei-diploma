package mg.yoan.diploma.repository;

import java.util.List;
import mg.yoan.diploma.repository.model.JCourse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JCourseRepository extends JpaRepository<JCourse, String> {

  List<JCourse> findAllByOrderByRefAsc();

  boolean existsByRefIgnoreCase(String ref);

  boolean existsByRefIgnoreCaseAndIdNot(String ref, String id);
}
