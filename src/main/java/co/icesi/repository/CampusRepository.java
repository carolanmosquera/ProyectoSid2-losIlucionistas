package co.icesi.repository;

import co.icesi.model.Campus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CampusRepository extends JpaRepository<Campus, Integer> {

    List<Campus> findByCityCode(Integer cityCode);

}