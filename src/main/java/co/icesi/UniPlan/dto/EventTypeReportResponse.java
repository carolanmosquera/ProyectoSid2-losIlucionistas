package co.icesi.UniPlan.dto;

public record EventTypeReportResponse(
        String type,
        long totalEvents,
        long totalEnrollments,
        double averageOccupancyPercentage) {
}
