package co.icesi.UniPlan.model.mongo;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "Roles")
public class Role {

    @Id
    private String id;

    private String description;

    private String name;

    private List<Permission> permissions;
}
