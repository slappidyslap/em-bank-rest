package kg.musabaev.em_bank_rest.dto;

import kg.musabaev.em_bank_rest.entity.CardStatus;
import lombok.Builder;

import java.time.LocalDate;

@Builder
public record GetCreatePatchCardResponse(
        Long id,
        String numberMasked,
        LocalDate expiry,
        String owner,
        CardStatus status) {
}