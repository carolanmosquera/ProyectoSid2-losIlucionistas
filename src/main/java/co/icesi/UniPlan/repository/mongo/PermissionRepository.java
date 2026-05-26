package co.icesi.UniPlan.repository.mongo;

import org.springframework.data.mongodb.repository.MongoRepository;

import co.icesi.UniPlan.model.mongo.Permission;
import java.util.Optional;

public interface PermissionRepository extends MongoRepository<Permission, String> {

    Optional findById(String id);

    Permission findByResource(String resource);

}
