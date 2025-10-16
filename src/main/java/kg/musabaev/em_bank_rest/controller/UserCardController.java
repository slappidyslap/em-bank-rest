package kg.musabaev.em_bank_rest.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import kg.musabaev.em_bank_rest.dto.GetCreatePatchCardResponse;
import kg.musabaev.em_bank_rest.util.Pair;
import kg.musabaev.em_bank_rest.dto.TransferBetweenCardsRequest;
import kg.musabaev.em_bank_rest.entity.Card;
import kg.musabaev.em_bank_rest.service.impl.SimpleCardService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/v1/users/me/cards")
@RequiredArgsConstructor
@Valid
public class UserCardController {

    private final SimpleCardService cardService;

    @GetMapping
    public ResponseEntity<Page<GetCreatePatchCardResponse>> getMyAllCards(
            Specification<Card> filter,
            Pageable pageable,
            @AuthenticationPrincipal Authentication auth) {
        return ResponseEntity.ok(cardService.getAllCards(filter, pageable, auth));
    }

    @GetMapping("/{cardId}")
    public ResponseEntity<GetCreatePatchCardResponse> getById(
            @Positive(message = "{app.msg.positive}") @PathVariable Long cardId,
            @AuthenticationPrincipal Authentication auth) {
        return ResponseEntity.ok(cardService.getById(cardId, auth));
    }

    @GetMapping("/{cardId}/balance")
    public ResponseEntity<Pair<BigDecimal>> getBalance(
            @Positive(message = "{app.msg.positive}") @PathVariable Long cardId,
            @AuthenticationPrincipal Authentication auth) {
        return ResponseEntity.ok(cardService.getCardBalance(cardId, auth));
    }

    @PostMapping("/{cardId}/request-block")
    public ResponseEntity<?> requestBlock(
            @Positive(message = "{app.msg.positive}") @PathVariable Long cardId,
            @AuthenticationPrincipal Authentication auth) {
        cardService.requestBlockCard(cardId, auth);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/transfer")
    public ResponseEntity<?> transferMoney(
            @Valid @RequestBody TransferBetweenCardsRequest dto,
            @AuthenticationPrincipal Authentication auth) {
        cardService.transferMoney(auth, dto);
        return ResponseEntity.noContent().build();
    }
}
