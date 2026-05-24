package co.icesi.UniPlan.dto;

import jakarta.validation.constraints.NotBlank;

public record RegisterExportRequest(
        @NotBlank String exportedBy,
        @NotBlank String format) {
}