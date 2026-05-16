package co.icesi.repository;

import co.icesi.model.Program;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProgramRepository extends JpaRepository<Program, Integer> {

    List<Program> findByAreaCode(Integer areaCode);

}