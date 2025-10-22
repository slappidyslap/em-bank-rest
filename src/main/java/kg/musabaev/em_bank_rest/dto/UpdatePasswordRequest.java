package kg.musabaev.em_bank_rest.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UpdatePasswordRequest(
        @NotNull @NotBlank String oldPassword,
        @NotNull @NotBlank String newPassword
) {
}
