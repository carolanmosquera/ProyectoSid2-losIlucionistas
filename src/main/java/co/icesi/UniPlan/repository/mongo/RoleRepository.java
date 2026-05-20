package co.icesi.UniPlan.repository.mongo;

import co.icesi.UniPlan.model.mongo.Role;
import java.util.Optional;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface RoleRepository extends MongoRepository<Role, String> {

    Optional<Role> findByName(String name);
}
