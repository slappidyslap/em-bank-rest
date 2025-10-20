package kg.musabaev.em_bank_rest.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import kg.musabaev.em_bank_rest.dto.GetCreatePatchCardResponse;
import kg.musabaev.em_bank_rest.dto.TransferBetweenCardsRequest;
import kg.musabaev.em_bank_rest.repository.specification.CardSpecification;
import kg.musabaev.em_bank_rest.security.SimpleUserDetails;
import kg.musabaev.em_bank_rest.service.impl.SimpleCardService;
import kg.musabaev.em_bank_rest.util.Pair;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/v1/users/me/cards")
@RequiredArgsConstructor
@Valid
public class UserCardController {

    private final SimpleCardService cardService;

    @GetMapping
    public PagedModel<GetCreatePatchCardResponse> getMyAllCards(
            @ModelAttribute CardSpecification filters,
            Pageable pageable,
            @AuthenticationPrincipal SimpleUserDetails userDetails) {
        return cardService.getAll(filters, pageable, userDetails);
    }

    @GetMapping("/{cardId}")
    public GetCreatePatchCardResponse getById(
            @Positive @PathVariable Long cardId,
            @AuthenticationPrincipal SimpleUserDetails userDetails) {
        return cardService.getById(cardId, userDetails);
    }

    @GetMapping("/{cardId}/balance")
    public Pair<BigDecimal> getBalance(
            @Positive @PathVariable Long cardId,
            @AuthenticationPrincipal SimpleUserDetails userDetails) {
        return cardService.getBalance(cardId, userDetails);
    }

    @PostMapping("/{cardId}/request-block")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void requestBlock(
            @Positive @PathVariable Long cardId,
            @AuthenticationPrincipal SimpleUserDetails userDetails) {
        cardService.requestBlockCard(cardId, userDetails);
    }

    @PostMapping("/transfer")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void transferMoney(
            @Valid @RequestBody TransferBetweenCardsRequest dto,
            @AuthenticationPrincipal SimpleUserDetails userDetails) {
        cardService.transferMoney(userDetails, dto);
    }
}
