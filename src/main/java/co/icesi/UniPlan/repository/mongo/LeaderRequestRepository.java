// src/main/java/co/icesi/UniPlan/repository/mongo/LeaderRequestRepository.java
package co.icesi.UniPlan.repository.mongo;

import co.icesi.UniPlan.model.mongo.LeaderRequest;
import java.util.List;
import java.util.Optional;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface LeaderRequestRepository extends MongoRepository<LeaderRequest, String> {

    List<LeaderRequest> findByStatus(String status);

    Optional<LeaderRequest> findByAppUserId(String appUserId);

    boolean existsByAppUserIdAndStatus(String appUserId, String status);
}