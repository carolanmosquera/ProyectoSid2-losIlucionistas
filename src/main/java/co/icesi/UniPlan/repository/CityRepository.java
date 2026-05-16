package co.icesi.UniPlan.repository;

import co.icesi.UniPlan.model.City;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CityRepository extends JpaRepository<City, Integer> {

    List<City> findByDepartmentCode(Integer deptCode);

}