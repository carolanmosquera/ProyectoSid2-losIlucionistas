package co.icesi.UniPlan.dto;

import jakarta.validation.constraints.NotBlank;

public record EventEnrollmentRequest(
        @NotBlank String institutionalId) {
}
