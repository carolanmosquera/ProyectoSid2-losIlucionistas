package co.icesi.UniPlan.repository.mongo;

import co.icesi.UniPlan.model.mongo.EventStatistics;
import java.util.Optional;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface EventStatisticsRepository extends MongoRepository<EventStatistics, String> {

    Optional<EventStatistics> findByEventId(String eventId);

    Optional<EventStatistics> findByEventCode(String eventCode);

    boolean existsByEventId(String eventId);
}