package kg.musabaev.em_bank_rest.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record CreateCardRequest(@NotNull @Positive Long userId) {

}
