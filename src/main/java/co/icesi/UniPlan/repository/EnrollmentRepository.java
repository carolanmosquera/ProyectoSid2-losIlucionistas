package co.icesi.UniPlan.repository;

import co.icesi.UniPlan.model.Enrollment;
import co.icesi.UniPlan.model.EnrollmentId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EnrollmentRepository extends JpaRepository<Enrollment, EnrollmentId> {

    List<Enrollment> findByIdStudentId(String studentId);

}
