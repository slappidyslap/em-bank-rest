package kg.musabaev.em_bank_rest.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import kg.musabaev.em_bank_rest.entity.User;

public record SignupUserRequest(
        @NotNull(message = "{app.msg.not_null}")
        @NotBlank(message = "{app.msg.not_blank}")
        String fullName,
        @NotNull(message = "{app.msg.not_null}")
        @Email(message = "{app.msg.email}")
        @NotBlank(message = "{app.msg.not_blank}")
        String email,
        @NotNull(message = "{app.msg.not_null}")
        @NotBlank(message = "{app.msg.not_blank}")
        String password) {
}