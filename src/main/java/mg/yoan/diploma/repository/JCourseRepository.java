package mg.yoan.diploma.repository;

import mg.yoan.diploma.repository.model.JCourse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JCourseRepository extends JpaRepository<JCourse, String> {}
