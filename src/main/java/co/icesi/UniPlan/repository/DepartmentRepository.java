package co.icesi.UniPlan.repository;

import co.icesi.UniPlan.model.Department;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DepartmentRepository extends JpaRepository<Department, Integer> {

    List<Department> findByCountryCode(Integer countryCode);

}