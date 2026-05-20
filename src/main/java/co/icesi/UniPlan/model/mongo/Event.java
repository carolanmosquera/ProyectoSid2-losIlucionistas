package co.icesi.UniPlan.model.mongo;

import java.time.Instant;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import co.icesi.UniPlan.model.mongo.Inscription;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "Events")
public class Event {

    @Id
    private String id;

    @Field("available_slots")
    private Integer availableSlots;

    @Field("created_at")
    private Instant createdAt;

    private String description;

    @Field("end_date")
    private Instant endDate;

    @Field("event_code")
    private String eventCode;

    @Field("event_details")
    private EventDetails eventDetails;

    @Field("sport_type")
    private String sportType;

    @Field("teams_quantity")
    private Integer teamsQuantity;

    @Field("total_hours")
    private String totalHours;

    @Field("tournament_format")
    private List<String> tournamentFormat;

    @Field("tournament_type")
    private String tournamentType;

    private List<Inscription> inscriptions;

    private String location;

    @Field("max_slots")
    private Integer maxSlots;

    @Field("organization_id")
    private String organizationId;

    @Field("organizer_id")
    private String organizerId;

    @Field("organizer_type")
    private String organizerType;

    @Field("start_date")
    private Instant startDate;

    private String status;

    private String title;

    private String type;

    @Field("updated_at")
    private Instant updatedAt;
}
