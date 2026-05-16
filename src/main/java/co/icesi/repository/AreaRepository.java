package co.icesi.repository;

import co.icesi.model.Area;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AreaRepository extends JpaRepository<Area, Integer> {

    List<Area> findByFacultyCode(Integer facultyCode);

}
