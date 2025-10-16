package kg.musabaev.em_bank_rest.dto;

import kg.musabaev.em_bank_rest.entity.CardStatus;

public record UpdateStatusCardRequest(CardStatus status) {
}
