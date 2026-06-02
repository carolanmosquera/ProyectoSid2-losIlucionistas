package co.icesi.UniPlan.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record AppUserRegistrationRequest(
        @NotBlank String institutionalId,
        @NotBlank @Email String institutionalEmail,
        @NotBlank String passwordHash,
        String roleId,
        String userType) {
}
