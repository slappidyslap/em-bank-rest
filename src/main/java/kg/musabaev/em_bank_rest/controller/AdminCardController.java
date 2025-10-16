package kg.musabaev.em_bank_rest.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import kg.musabaev.em_bank_rest.dto.CreateCardRequest;
import kg.musabaev.em_bank_rest.dto.GetCreatePatchCardResponse;
import kg.musabaev.em_bank_rest.dto.UpdateStatusCardRequest;
import kg.musabaev.em_bank_rest.entity.Card;
import kg.musabaev.em_bank_rest.repository.specification.CardSpecification;
import kg.musabaev.em_bank_rest.service.CardService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/cards")
@RequiredArgsConstructor
@Valid
public class AdminCardController {

    private final CardService cardService;

    @PostMapping
    public ResponseEntity<GetCreatePatchCardResponse> createCard(
            @Valid @RequestBody CreateCardRequest dto) {
        return ResponseEntity.accepted().body(cardService.create(dto.userId()));
    }

    @GetMapping
    public ResponseEntity<Page<GetCreatePatchCardResponse>> getAllCards(
            String status,
            @Positive(message = "{app.msg.positive}") Long userId,
            Pageable pageable) {
        Specification<Card> spec = CardSpecification.build(status, userId); // fixme
        return ResponseEntity.ok(cardService.getAllCards(spec, pageable));
    }

    @GetMapping("/{cardId}")
    public ResponseEntity<GetCreatePatchCardResponse> getCardById(
            @Positive(message = "{app.msg.positive}") Long cardId) {
        return ResponseEntity.ok(cardService.getById(cardId));
    }

    @DeleteMapping("/{cardId}")
    public ResponseEntity<?> deleteCard(
            @Positive(message = "{app.msg.positive}") @PathVariable Long cardId) {
        cardService.delete(cardId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}")
    public ResponseEntity<GetCreatePatchCardResponse> updateCardStatus(@PathVariable Long id, @RequestBody UpdateStatusCardRequest dto) { //todo valid
        return ResponseEntity.ok(cardService.patchStatus(id, dto));
    }
}
