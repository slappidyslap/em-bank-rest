package kg.musabaev.em_bank_rest.dto;

import kg.musabaev.em_bank_rest.entity.CardStatus;

import java.time.LocalDate;

public record GetCardDetailsResponse(
        Long id,
        String number,
        LocalDate expiry,
        String owner,
        CardStatus status
) {
}
