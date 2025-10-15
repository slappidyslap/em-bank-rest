package kg.musabaev.em_bank_rest.dto;

import jakarta.annotation.Nonnull;
import jakarta.validation.constraints.Positive;

public record CreateCardRequest(@Nonnull @Positive Long userId) {

}
