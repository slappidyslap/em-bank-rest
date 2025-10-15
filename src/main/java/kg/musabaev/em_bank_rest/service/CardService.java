package kg.musabaev.em_bank_rest.service;

import kg.musabaev.em_bank_rest.dto.GetCreateSingleCardResponse;
import kg.musabaev.em_bank_rest.entity.Card;
import kg.musabaev.em_bank_rest.entity.CardStatus;
import kg.musabaev.em_bank_rest.entity.User;
import kg.musabaev.em_bank_rest.exception.CardNotFoundException;
import kg.musabaev.em_bank_rest.mapper.CardMapper;
import kg.musabaev.em_bank_rest.repository.CardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
public class CardService {

    private final CardRepository cardRepository;
    private final CardMapper cardMapper;
    private final UserService userService;

    @Transactional
    public GetCreateSingleCardResponse create(Long userId) { // FIXME
        var newCard = new Card();
        newCard.setNumber("************" + ThreadLocalRandom.current().nextInt(1000, 9999));
        newCard.setExpiry(LocalDate.now().plusYears(3));
        newCard.setStatus(CardStatus.ACTIVE);
        newCard.setBalance(0d);
        var assignedUser = userService.getById(userId);
        newCard.setUser(assignedUser);
        newCard.setOwner(assignedUser.getFullName());

        var persistedCard = cardRepository.save(newCard);
        return cardMapper.toCreateCardResponse(persistedCard);
    }

    public GetCreateSingleCardResponse getById(Long id) {
        var foundCard = cardRepository.findById(id)
                .orElseThrow(() -> new CardNotFoundException(id));
        return cardMapper.toGetSingleCardResponse(foundCard);
    }

    public Page<GetCreateSingleCardResponse> getAllCards(Pageable pageable) {
        var cards = cardRepository.findAll(pageable);
        return cards.map(cardMapper::toGetSingleCardResponse);
    }

    @Transactional
    public Card updateStatus(Long cardId, CardStatus newStatus) {
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new CardNotFoundException(cardId));
        card.setStatus(newStatus);
        return cardRepository.save(card);
    }

    // user

    public Page<Card> getUserCards(User user, Pageable pageable/*, User authorizedUser*/) {
        return cardRepository.findAllByOwner(user, pageable);
    }

    public Double getCardBalance(Long cardId) {
        var card = cardRepository.findById(cardId)
                .orElseThrow(() -> new CardNotFoundException(cardId));
//        if (card.getStatus() == CardStatus.BLOCKED) todo можно ли так делать
        return card.getBalance();
    }

    @Transactional
    public void blockCard(Long cardId) {
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new CardNotFoundException(cardId));

        if (card.getStatus() == CardStatus.BLOCKED)
            throw new IllegalStateException("couldn't block already blocked card");
        card.setStatus(CardStatus.BLOCKED);
        cardRepository.save(card);
    }

    public void delete(Long id) {
        cardRepository.deleteById(id);
    }
}
