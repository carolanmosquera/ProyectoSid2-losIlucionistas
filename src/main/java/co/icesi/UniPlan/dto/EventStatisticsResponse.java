package co.icesi.UniPlan.dto;

public record EventStatisticsResponse(
        String eventId,
        String eventCode,
        long enrolled,
        long cancellations,
        long attendees,
        double occupancyPercentage) {
}
