package co.icesi.UniPlan.repository;

import co.icesi.UniPlan.model.Campus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CampusRepository extends JpaRepository<Campus, Integer> {

    List<Campus> findByCityCode(Integer cityCode);

}