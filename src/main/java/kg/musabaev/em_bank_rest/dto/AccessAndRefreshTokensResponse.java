package kg.musabaev.em_bank_rest.dto;

import lombok.Builder;

@Builder
public record AccessAndRefreshTokensResponse(String accessToken, String refreshToken) {
}
