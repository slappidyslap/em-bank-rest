package kg.musabaev.em_bank_rest.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.math.BigDecimal;

public record TransferBetweenCardsRequest(
        @NotNull(message = "{app.msg.not_null}")
        @Pattern(regexp = "[0-9]{16}", message = "{app.msg.pattern.16_digits}")
        String fromCardNumber,

        @NotNull(message = "{app.msg.not_null}")
        @Pattern(regexp = "[0-9]{16}", message = "{app.msg.pattern.16_digits}")
        String toCardNumber,

        @NotNull(message = "{app.msg.not_null}")
        @DecimalMin(value = "1", message = "{app.msg.min_1}")
        BigDecimal amount
) {
}
