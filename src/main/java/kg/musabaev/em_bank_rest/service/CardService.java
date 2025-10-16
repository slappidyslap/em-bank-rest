package kg.musabaev.em_bank_rest.service;

import kg.musabaev.em_bank_rest.dto.GetCreatePatchCardResponse;
import kg.musabaev.em_bank_rest.dto.TransferBetweenCardsRequest;
import kg.musabaev.em_bank_rest.dto.UpdateStatusCardRequest;
import kg.musabaev.em_bank_rest.repository.specification.CardSpecification;
import kg.musabaev.em_bank_rest.util.Pair;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;

import java.math.BigDecimal;

public interface CardService {

    GetCreatePatchCardResponse create(Long userId);

    GetCreatePatchCardResponse getById(Long id);

    Page<GetCreatePatchCardResponse> getAll(CardSpecification spec, Pageable pageable);

    GetCreatePatchCardResponse patchStatus(Long cardId, UpdateStatusCardRequest newStatus);

    Page<GetCreatePatchCardResponse> getAll(CardSpecification filter, Pageable pageable, Authentication auth);

    void delete(Long id);

    void transferMoney(Authentication auth, TransferBetweenCardsRequest dto);

    GetCreatePatchCardResponse getById(Long cardId, Authentication auth);

    Pair<BigDecimal> getBalance(Long cardId, Authentication auth);

    void requestBlockCard(Long cardId, Authentication auth);
}
