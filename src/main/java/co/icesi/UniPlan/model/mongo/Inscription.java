package co.icesi.UniPlan.model.mongo;

import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Field;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Inscription {

    private Boolean attended;

    @Field("cancelled_at")
    private Instant cancelledAt;

    @Field("enrolled_at")
    private Instant enrolledAt;

    @Field("institutional_id")
    private String institutionalId;

    private String status;
}
