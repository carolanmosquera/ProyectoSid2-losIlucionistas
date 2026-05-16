package co.icesi.UniPlan.repository;

import co.icesi.UniPlan.model.Group;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GroupRepository extends JpaRepository<Group, String> {

    List<Group> findBySemester(String semester);

}