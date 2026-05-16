package co.icesi.UniPlan.repository;

import co.icesi.UniPlan.model.Area;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AreaRepository extends JpaRepository<Area, Integer> {

    List<Area> findByFaculty_Code(Integer code);

}
