package kg.musabaev.em_bank_rest.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record CreateCardRequest(
        @NotNull(message = "{app.msg.not_null}")
        @Positive(message = "{app.msg.positive}")
        Long userId) {

}
