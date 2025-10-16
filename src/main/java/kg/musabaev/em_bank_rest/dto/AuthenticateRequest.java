package kg.musabaev.em_bank_rest.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AuthenticateRequest(
        @NotNull(message = "{app.msg.not_null}")
        @Email(message = "{app.msg.email}")
        @NotBlank(message = "{app.msg.not_blank}")
        String email,
        @NotNull(message = "{app.msg.not_null}")
        @NotBlank(message = "{app.msg.not_blank}")
        String password
) {
}
