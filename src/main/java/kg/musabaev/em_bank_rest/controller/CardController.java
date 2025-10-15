package kg.musabaev.em_bank_rest.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import kg.musabaev.em_bank_rest.dto.CreateCardRequest;
import kg.musabaev.em_bank_rest.dto.GetCreateSingleCardResponse;
import kg.musabaev.em_bank_rest.entity.Card;
import kg.musabaev.em_bank_rest.repository.specification.CardSpecification;
import kg.musabaev.em_bank_rest.service.impl.SimpleCardService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/v1/cards")
@RequiredArgsConstructor
public class CardController {

    private final SimpleCardService cardService;

    // админ
    @PostMapping
    public ResponseEntity<GetCreateSingleCardResponse> create(
            @Valid @RequestBody CreateCardRequest dto) {
        return ResponseEntity.accepted().body(cardService.create(dto.userId()));
    }

    @GetMapping("/{cardId}")
    public ResponseEntity<GetCreateSingleCardResponse> getById(
            @Valid @Positive(message = "{app.msg.card_id_positive}") @PathVariable Long cardId) {
        return ResponseEntity.ok(cardService.getById(cardId));
    }

    @GetMapping
    public ResponseEntity<Page<GetCreateSingleCardResponse>> getAll(
            String status,
            @Valid @Positive(message = "{app.msg.user_id_positive}") Long userId,
            Pageable pageable) {
        Specification<Card> spec = CardSpecification.build(status, userId);
        return ResponseEntity.ok(cardService.getAllCards(spec, pageable));
    }

    // админ
    @DeleteMapping("/{cardId}")
    public ResponseEntity<?> delete(
            @Valid @Positive(message = "{app.msg.card_id_positive}") @PathVariable Long cardId) {
        cardService.delete(cardId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{cardId}/block-request")
    public ResponseEntity<?> requestBlock(
            @Valid @Positive(message = "{app.msg.card_id_positive}") @PathVariable Long cardId) {
        cardService.blockCard(cardId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{cardId}/balance")
    public ResponseEntity<BigDecimal> getBalance(
            @Valid @Positive(message = "{app.msg.card_id_positive}") @PathVariable Long cardId) {
        return ResponseEntity.ok(cardService.getCardBalance(cardId));
    }
}
