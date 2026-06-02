package co.icesi.UniPlan.model.mongo;

import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "AppUser")
public class AppUser {

    @Id
    private String id;

    @Field("created_at")
    private Instant createdAt;

    @Field("institutional_email")
    private String institutionalEmail;

    @Field("institutional_id")
    private String institutionalId;

    @Field("last_login")
    private Instant lastLogin;

    @Field("password_hash")
    private String passwordHash;

    @Field("role_id")
    private String roleId;

    private String status;

    @Field("user_type")
    private String userType;
}
