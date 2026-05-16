package co.icesi.UniPlan.repository;

import co.icesi.UniPlan.model.Faculty;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FacultyRepository extends JpaRepository<Faculty, Integer> {

    Optional<Faculty> findByDeanId(String deanId);

}