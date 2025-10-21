package kg.musabaev.em_bank_rest.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AuthenticateRequest(
        @NotNull 
        @Email 
        @NotBlank 
        String email,
        @NotNull 
        @NotBlank 
        String password
) {
}
