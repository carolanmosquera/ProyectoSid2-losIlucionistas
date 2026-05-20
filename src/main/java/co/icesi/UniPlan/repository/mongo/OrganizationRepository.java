package co.icesi.UniPlan.repository.mongo;

import co.icesi.UniPlan.model.mongo.Organization;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface OrganizationRepository extends MongoRepository<Organization, String> {
}
