package co.icesi.UniPlan.repository.mongo;

import co.icesi.UniPlan.model.mongo.Event;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface EventRepository extends MongoRepository<Event, String> {

    List<Event> findByType(String type);

    List<Event> findByStatus(String status);

    List<Event> findByOrganizationId(String organizationId);

    List<Event> findByOrganizerId(String organizerId);

    List<Event> findByStartDateBetween(Instant start, Instant end);

    Optional<Event> findByEventCode(String eventCode);

    boolean existsByEventCode(String eventCode);
}
