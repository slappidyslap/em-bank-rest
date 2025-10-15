package kg.musabaev.em_bank_rest.service;

import kg.musabaev.em_bank_rest.dto.GetCreateSingleCardResponse;
import kg.musabaev.em_bank_rest.entity.Card;
import kg.musabaev.em_bank_rest.entity.CardStatus;
import kg.musabaev.em_bank_rest.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CardService {

    GetCreateSingleCardResponse create(Long userId);

    GetCreateSingleCardResponse getById(Long id);

    Page<GetCreateSingleCardResponse> getAllCards(Pageable pageable);

    Card updateStatus(Long cardId, CardStatus newStatus);

    Page<Card> getUserCards(User user, Pageable pageable/*, User authorizedUser*/);

    Double getCardBalance(Long cardId);

    void blockCard(Long cardId);

    void delete(Long id);
}
