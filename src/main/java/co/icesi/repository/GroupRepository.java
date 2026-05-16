package co.icesi.repository;

import co.icesi.model.Group;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GroupRepository extends JpaRepository<Group, String> {

    List<Group> findBySemester(String semester);

}