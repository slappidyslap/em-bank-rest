package kg.musabaev.em_bank_rest.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import kg.musabaev.em_bank_rest.dto.CreateCardRequest;
import kg.musabaev.em_bank_rest.dto.GetCreateSingleCardResponse;
import kg.musabaev.em_bank_rest.dto.TransferBetweenCardsRequest;
import kg.musabaev.em_bank_rest.entity.Card;
import kg.musabaev.em_bank_rest.entity.User;
import kg.musabaev.em_bank_rest.repository.specification.CardSpecification;
import kg.musabaev.em_bank_rest.service.impl.SimpleCardService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/v1/cards")
@RequiredArgsConstructor
@Valid
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
            @Positive(message = "{app.msg.positive}") @PathVariable Long cardId) {
        return ResponseEntity.ok(cardService.getById(cardId));
    }

    @GetMapping
    public ResponseEntity<Page<GetCreateSingleCardResponse>> getAll(
            String status,
            @Positive(message = "{app.msg.positive}") Long userId,
            Pageable pageable) {
        Specification<Card> spec = CardSpecification.build(status, userId); // fixme
        return ResponseEntity.ok(cardService.getAllCards(spec, pageable));
    }

    // админ
    @DeleteMapping("/{cardId}")
    public ResponseEntity<?> delete(
            @Positive(message = "{app.msg.positive}") @PathVariable Long cardId) {
        cardService.delete(cardId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{cardId}/block-request")
    public ResponseEntity<?> requestBlock(
            @Positive(message = "{app.msg.positive}") @PathVariable Long cardId) {
        cardService.blockCard(cardId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{cardId}/balance")
    public ResponseEntity<BigDecimal> getBalance(
            @Positive(message = "{app.msg.positive}") @PathVariable Long cardId) {
        return ResponseEntity.ok(cardService.getCardBalance(cardId));
    }

    @PostMapping("/transfer")
    public ResponseEntity<?> transferMoney(
            @Valid @RequestBody TransferBetweenCardsRequest dto) {
        cardService.transferMoney(User.builder().id(1L).build(), dto);
        return ResponseEntity.noContent().build(); // FIXME
    }
}
