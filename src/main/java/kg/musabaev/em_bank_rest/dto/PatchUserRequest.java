package kg.musabaev.em_bank_rest.dto;

import jakarta.validation.constraints.NotBlank;
import kg.musabaev.em_bank_rest.util.constraint.EmailOrNull;

public record PatchUserRequest(
        @NotBlank(message = "{app.msg.not_blank}")
        String fullName,
        @NotBlank(message = "{app.msg.not_blank}")
        @EmailOrNull // todo проверить
        String email,
        @NotBlank(message = "{app.msg.not_blank}")
        String password) {
}