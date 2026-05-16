package co.icesi.UniPlan.repository;

import co.icesi.UniPlan.model.Subject;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SubjectRepository extends JpaRepository<Subject, String> {

    List<Subject> findByProgramCode(Integer programCode);

}