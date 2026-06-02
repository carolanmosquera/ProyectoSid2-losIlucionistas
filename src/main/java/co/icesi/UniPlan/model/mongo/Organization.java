package co.icesi.UniPlan.model.mongo;

import java.util.List;
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
@Document(collection = "Organizations")
public class Organization {

    @Id
    private String id;

    private String description;

    @Field("inCharge_students")
    private List<InChargeStudent> inChargeStudents;

    private String name;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class InChargeStudent {

        @Field("student_id")
        private String studentId;
    }
}
