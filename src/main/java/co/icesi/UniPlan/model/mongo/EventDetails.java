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
public class EventDetails {

    @Field("helped_community")
    private String helpedCommunity;

    @Field("logistic_info")
    private String logisticInfo;

    @Field("min_semester")
    private Integer minSemester;

    @Field("player_per_team")
    private Integer playerPerTeam;

    private String prerequisites;

    private String reason;

    @Field("required_materials")
    private List<String> requiredMaterials;

    @Field("speaker_info")
    private SpeakerInfo speakerInfo;
}
