package mg.yoan.diploma.repository;

import java.util.List;
import java.util.Optional;
import mg.yoan.diploma.repository.model.JTeacher;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface JTeacherRepository extends JpaRepository<JTeacher, String> {

  Optional<JTeacher> findByEmployeeNumber(String employeeNumber);

  boolean existsByEmployeeNumber(String employeeNumber);

  boolean existsByEmployeeNumberAndIdNot(String employeeNumber, String id);

  @Query(
      """
      select distinct t from JTeacher t
      join fetch t.user
      order by t.employeeNumber
      """)
  List<JTeacher> findAllDetailed();

  @Query(
      """
      select t from JTeacher t
      join fetch t.user
      where t.id = :id
      """)
  Optional<JTeacher> findDetailedById(@Param("id") String id);
}
