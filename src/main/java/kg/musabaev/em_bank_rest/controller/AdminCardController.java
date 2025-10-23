package kg.musabaev.em_bank_rest.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import kg.musabaev.em_bank_rest.dto.CreateCardRequest;
import kg.musabaev.em_bank_rest.dto.GetCreatePatchCardResponse;
import kg.musabaev.em_bank_rest.dto.UpdateStatusCardRequest;
import kg.musabaev.em_bank_rest.repository.specification.CardSpecification;
import kg.musabaev.em_bank_rest.security.SimpleUserDetails;
import kg.musabaev.em_bank_rest.service.CardService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/cards")
@RequiredArgsConstructor
@Valid
public class AdminCardController {

    private final CardService cardService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public GetCreatePatchCardResponse createCard(
            @Valid @RequestBody CreateCardRequest dto) {
        return cardService.create(dto);
    }

    @GetMapping
    public PagedModel<GetCreatePatchCardResponse> getAllCards(
            @ModelAttribute CardSpecification filters,
            Pageable pageable) {
        return cardService.getAllForAdmin(filters, pageable);
    }

    @GetMapping("/{cardId}")
    public GetCreatePatchCardResponse getCardById(
            @Positive @PathVariable Long cardId) {
        return cardService.getByIdForAdmin(cardId);
    }

    @DeleteMapping("/{cardId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCard(
            @Positive @PathVariable Long cardId) {
        cardService.delete(cardId);
    }

    @PatchMapping("/{cardId}")
    public GetCreatePatchCardResponse updateCardStatus(
            @Positive @PathVariable Long cardId,
            @RequestBody UpdateStatusCardRequest dto,
            @AuthenticationPrincipal SimpleUserDetails userDetails) {
        return cardService.patchStatus(cardId, dto, userDetails);
    }
}
