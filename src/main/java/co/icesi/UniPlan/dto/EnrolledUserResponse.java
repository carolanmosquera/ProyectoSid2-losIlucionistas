package co.icesi.UniPlan.dto;

import java.time.Instant;

public record EnrolledUserResponse(
        String institutionalId,
        String firstName,
        String lastName,
        String email,
        String status,
        Boolean attended,
        Instant enrolledAt,
        Instant cancelledAt) {
}
