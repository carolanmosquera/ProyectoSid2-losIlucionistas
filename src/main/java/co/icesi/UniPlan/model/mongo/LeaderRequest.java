// src/main/java/co/icesi/UniPlan/model/mongo/LeaderRequest.java
package co.icesi.UniPlan.model.mongo;

import java.time.Instant;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "LeaderRequests")
public class LeaderRequest {

    @Id
    private String id;

    @Field("app_user_id")
    private String appUserId;// _id del AppUser en MongoDB

    @Field("institutional_id")
    private String institutionalId;

    @Field("institutional_email")
    private String institutionalEmail;

    private String status;// "PENDING", "APPROVED", "REJECTED"

    @Field("requested_at")
    private Instant requestedAt;

    @Field("resolved_at")
    private Instant resolvedAt;

    @Field("resolved_by")
    private String resolvedBy;// email del admin que resolvió
}