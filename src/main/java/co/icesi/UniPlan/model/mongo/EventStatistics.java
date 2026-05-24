package co.icesi.UniPlan.model.mongo;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "EventStatistics")
public class EventStatistics {

    @Id
    private String id;

    /** Referencia al _id del documento en Events */
    @Indexed(unique = true)
    @Field("event_id")
    private String eventId;

    /** Referencia al event_code del documento en Events */
    @Field("event_code")
    private String eventCode;

    /**
     * Fecha en que se alcanzó el mayor número de inscripciones activas simultáneas.
     * No se puede recalcular desde Events porque las inscripciones se cancelan
     * y se pierde el rastro del momento pico.
     */
    @Field("peak_inscriptions_date")
    private Instant peakInscriptionsDate;

    /**
     * Histórico de actividad diaria.
     * Imposible de reconstruir desde Events: una vez que pasa el día,
     * el array de inscripciones solo muestra el estado final.
     */
    @Field("daily_inscriptions")
    @Builder.Default
    private List<DailyRecord> dailyInscriptions = new ArrayList<>();

    /**
     * Auditoría de exportaciones de reportes.
     * No existe en Events; se registra aquí cada vez que alguien exporta.
     */
    @Field("report_exports")
    @Builder.Default
    private List<ReportExport> reportExports = new ArrayList<>();

    @Field("last_updated")
    private Instant lastUpdated;

    // ── Clases embebidas ──────────────────────────────────────────────────────

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DailyRecord {

        private String date;// "2025-04-01"

        @Field("new_inscriptions")
        private int newInscriptions;

        @Field("new_cancellations")
        private int newCancellations;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReportExport {

        @Field("exported_by")
        private String exportedBy; // AppUser._id

        @Field("exported_at")
        private Instant exportedAt;

        private String format; // PDF, CSV, EXCEL
    }
}