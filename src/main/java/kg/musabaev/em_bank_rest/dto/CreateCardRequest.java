package kg.musabaev.em_bank_rest.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record CreateCardRequest(
        @NotNull(message = "{app.msg.user_id_not_null}")
        @Positive(message = "{app.msg.user_id_positive}")
        Long userId) {

}
