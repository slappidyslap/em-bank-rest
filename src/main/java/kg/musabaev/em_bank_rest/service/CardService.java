package kg.musabaev.em_bank_rest.service;

import kg.musabaev.em_bank_rest.dto.GetCreatePatchCardResponse;
import kg.musabaev.em_bank_rest.dto.TransferBetweenCardsRequest;
import kg.musabaev.em_bank_rest.dto.UpdateStatusCardRequest;
import kg.musabaev.em_bank_rest.entity.Card;
import kg.musabaev.em_bank_rest.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;

public interface CardService {

    GetCreatePatchCardResponse create(Long userId);

    GetCreatePatchCardResponse getById(Long id);

    Page<GetCreatePatchCardResponse> getAllCards(Specification<Card> spec, Pageable pageable);

    GetCreatePatchCardResponse patchStatus(Long cardId, UpdateStatusCardRequest newStatus);

    Page<Card> getUserCards(User user, Pageable pageable/*, User authorizedUser*/);

    BigDecimal getCardBalance(Long cardId);

    void blockCard(Long cardId);

    void delete(Long id);

    void transferMoney(User user, TransferBetweenCardsRequest dto);
}
