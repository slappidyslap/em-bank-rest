package kg.musabaev.em_bank_rest.dto;

import kg.musabaev.em_bank_rest.entity.CardStatus;
import org.mapstruct.Mapping;

import java.time.LocalDate;

public record GetCreateSingleCardResponse(
        Long id,
        String numberMasked,
        LocalDate expiry,
        String owner,
        CardStatus status,
        Double balance) {
}