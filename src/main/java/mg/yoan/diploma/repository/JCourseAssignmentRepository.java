package mg.yoan.diploma.repository;

import java.util.List;
import mg.yoan.diploma.repository.model.JCourseAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface JCourseAssignmentRepository extends JpaRepository<JCourseAssignment, String> {

  List<JCourseAssignment> findByTeacherId(String teacherId);

  List<JCourseAssignment> findByCourseId(String courseId);

  List<JCourseAssignment> findByGroupId(String groupId);

  boolean existsByCourseIdAndTeacherId(String courseId, String teacherId);

  boolean existsByCourseIdAndGroupIdAndTeacherId(String courseId, String groupId, String teacherId);

  boolean existsByCourseId(String courseId);

  boolean existsByTeacherId(String teacherId);

  boolean existsByGroupId(String groupId);

  @Query(
      """
      select distinct a from JCourseAssignment a
      join fetch a.course
      join fetch a.group g
      left join fetch g.promotion
      join fetch a.teacher t
      join fetch t.user
      where a.course.id = :courseId
      order by t.employeeNumber, g.ref
      """)
  List<JCourseAssignment> findDetailedByCourseId(@Param("courseId") String courseId);

  @Query(
      """
      select distinct a from JCourseAssignment a
      join fetch a.course
      join fetch a.group g
      left join fetch g.promotion
      join fetch a.teacher
      where a.teacher.id = :teacherId
      order by a.course.ref, g.ref
      """)
  List<JCourseAssignment> findDetailedByTeacherId(@Param("teacherId") String teacherId);

  @Query(
      """
      select distinct a from JCourseAssignment a
      join fetch a.course
      join fetch a.group g
      where g.promotion.id = :promotionId
      """)
  List<JCourseAssignment> findByPromotionId(@Param("promotionId") String promotionId);
}
