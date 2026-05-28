package co.icesi.UniPlan.model.mongo;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import org.springframework.data.mongodb.core.mapping.Field;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Permission {

    @Field("resource")
    private String resource;

    @Field("actions")
    private List<String> actions;
}
