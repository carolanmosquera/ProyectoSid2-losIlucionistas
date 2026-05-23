package co.icesi.UniPlan.dto;

import java.time.Instant;
import java.util.List;

public record EventStatisticsDetailResponse(
        String id,
        String eventId,
        String eventCode,
        // Calculados en tiempo real desde Events
        long activeInscriptions,
        long totalCancellations,
        long totalAttended,
        double occupancyPercentage,
        double cancellationRate,
        // Solo en EventStatistics (histórico)
        Instant peakInscriptionsDate,
        List<DailyRecord> dailyInscriptions,
        List<ReportExport> reportExports,
        Instant lastUpdated) {

    public record DailyRecord(
            String date,
            int newInscriptions,
            int newCancellations) {}

    public record ReportExport(
            String exportedBy,
            Instant exportedAt,
            String format) {}
}