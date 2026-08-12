package mg.yoan.diploma.repository;

import java.util.List;
import mg.yoan.diploma.PojaGenerated;
import mg.yoan.diploma.repository.model.Dummy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@PojaGenerated
@Repository
public interface DummyRepository extends JpaRepository<Dummy, String> {

  @Override
  List<Dummy> findAll();
}
