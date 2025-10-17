package kg.musabaev.em_bank_rest.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import kg.musabaev.em_bank_rest.dto.CreateCardRequest;
import kg.musabaev.em_bank_rest.dto.GetCreatePatchCardResponse;
import kg.musabaev.em_bank_rest.dto.UpdateStatusCardRequest;
import kg.musabaev.em_bank_rest.repository.specification.CardSpecification;
import kg.musabaev.em_bank_rest.service.CardService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/cards")
@RequiredArgsConstructor
@Valid
public class AdminCardController {

    private final CardService cardService;

    @PostMapping
    @ResponseStatus(HttpStatus.ACCEPTED)
    public GetCreatePatchCardResponse createCard(
            @Valid @RequestBody CreateCardRequest dto) {
        return cardService.create(dto);
    }

    @GetMapping
    public PagedModel<GetCreatePatchCardResponse> getAllCards(
            @ModelAttribute CardSpecification filters,
            Pageable pageable) {
        return cardService.getAll(filters, pageable);
    }

    @GetMapping("/{cardId}")
    public GetCreatePatchCardResponse getCardById(
            @Positive(message = "{app.msg.positive}") Long cardId) {
        return cardService.getById(cardId);
    }

    @DeleteMapping("/{cardId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCard(
            @Positive(message = "{app.msg.positive}") @PathVariable Long cardId) {
        cardService.delete(cardId);
    }

    @PatchMapping("/{cardId}")
    public GetCreatePatchCardResponse updateCardStatus(
            @PathVariable Long cardId,
            @RequestBody UpdateStatusCardRequest dto,
            @AuthenticationPrincipal Authentication auth) { //todo valid
        return cardService.patchStatus(cardId, dto, auth);
    }
}
