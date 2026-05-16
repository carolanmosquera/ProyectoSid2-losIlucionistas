package co.icesi.UniPlan.repository;

import co.icesi.UniPlan.model.Program;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProgramRepository extends JpaRepository<Program, Integer> {

    List<Program> findByAreaCode(Integer areaCode);

}