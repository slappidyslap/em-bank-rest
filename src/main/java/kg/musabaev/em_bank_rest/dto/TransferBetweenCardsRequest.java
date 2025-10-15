package kg.musabaev.em_bank_rest.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.math.BigDecimal;

public record TransferBetweenCardsRequest(
        @NotNull(message = "fromCard must be not null")
        @Pattern(regexp = "[0-9]{16}", message = "fromCard must be a 16-digit number")
        String fromCardNumber,

        @NotNull(message = "toCard must be not null")
        @Pattern(regexp = "[0-9]{16}", message = "fromCard must be a 16-digit number")
        String toCardNumber,

        @NotNull(message = "amount must be not null")
        @DecimalMin(message = "amount must be greater than 1", value = "1")
        BigDecimal amount
) {
}
