package kg.musabaev.em_bank_rest.service;

import kg.musabaev.em_bank_rest.dto.CreateCardRequest;
import kg.musabaev.em_bank_rest.dto.GetCreatePatchCardResponse;
import kg.musabaev.em_bank_rest.dto.TransferBetweenCardsRequest;
import kg.musabaev.em_bank_rest.dto.UpdateStatusCardRequest;
import kg.musabaev.em_bank_rest.repository.specification.CardSpecification;
import kg.musabaev.em_bank_rest.security.SimpleUserDetails;
import kg.musabaev.em_bank_rest.util.Pair;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedModel;

import java.math.BigDecimal;

public interface CardService {

    GetCreatePatchCardResponse create(CreateCardRequest userId);

    GetCreatePatchCardResponse getById(Long id);

    PagedModel<GetCreatePatchCardResponse> getAll(CardSpecification spec, Pageable pageable);

    GetCreatePatchCardResponse patchStatus(Long cardId, UpdateStatusCardRequest newStatus, SimpleUserDetails userDetails);

    PagedModel<GetCreatePatchCardResponse> getAll(CardSpecification filter, Pageable pageable, SimpleUserDetails userDetails);

    void delete(Long id);

    void transferMoney(SimpleUserDetails userDetails, TransferBetweenCardsRequest dto);

    GetCreatePatchCardResponse getById(Long cardId, SimpleUserDetails userDetails);

    Pair<BigDecimal> getBalance(Long cardId, SimpleUserDetails userDetails);

    void requestBlockCard(Long cardId, SimpleUserDetails userDetails);
}
