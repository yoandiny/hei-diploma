package mg.yoan.diploma.repository;

import java.util.List;
import mg.yoan.diploma.repository.model.JCourseAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JCourseAssignmentRepository extends JpaRepository<JCourseAssignment, String> {

  List<JCourseAssignment> findByTeacherId(String teacherId);

  List<JCourseAssignment> findByCourseId(String courseId);

  List<JCourseAssignment> findByGroupId(String groupId);

  boolean existsByCourseIdAndTeacherId(String courseId, String teacherId);
}
