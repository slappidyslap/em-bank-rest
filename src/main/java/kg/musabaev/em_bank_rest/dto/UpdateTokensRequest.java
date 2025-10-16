package kg.musabaev.em_bank_rest.dto;

import org.hibernate.validator.constraints.UUID;

public record UpdateTokensRequest(
        @UUID
        String refreshToken
) {
}
