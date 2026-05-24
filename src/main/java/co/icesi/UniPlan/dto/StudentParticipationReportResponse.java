package co.icesi.UniPlan.dto;

public record StudentParticipationReportResponse(
        String studentId,
        String institutionalId,
        long activeEnrollments,
        long cancelledEnrollments,
        long attendedEvents) {
}
