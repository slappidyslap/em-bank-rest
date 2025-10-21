package kg.musabaev.em_bank_rest.dto;

import jakarta.validation.constraints.NotNull;
import org.hibernate.validator.constraints.UUID;

public record UpdateTokensRequest(
        @NotNull
        @UUID
        String refreshToken
) {
}
