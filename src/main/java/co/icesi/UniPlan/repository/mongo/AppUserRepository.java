package co.icesi.UniPlan.repository.mongo;

import co.icesi.UniPlan.model.mongo.AppUser;
import java.util.Optional;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface AppUserRepository extends MongoRepository<AppUser, String> {

    Optional<AppUser> findByInstitutionalEmail(String institutionalEmail);

    Optional<AppUser> findByInstitutionalId(String institutionalId);

    boolean existsByInstitutionalId(String institutionalId);

    boolean existsByInstitutionalEmail(String institutionalEmail);
}
