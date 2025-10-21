package kg.musabaev.em_bank_rest.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.math.BigDecimal;

public record TransferBetweenCardsRequest(
        @NotNull
        @Pattern(regexp = "[0-9]{16}")
        String fromCardNumber,

        @NotNull
        @Pattern(regexp = "[0-9]{16}")
        String toCardNumber,

        @NotNull
        @DecimalMin(value = "1")
        BigDecimal amount
) {
}
