package kg.musabaev.em_bank_rest.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record PatchUserRequest(
        @NotBlank
        String fullName,
        @NotBlank
        @Email
        String email,
        @NotBlank
        String password) {
}