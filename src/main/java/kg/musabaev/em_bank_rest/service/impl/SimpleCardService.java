package kg.musabaev.em_bank_rest.service.impl;

import kg.musabaev.em_bank_rest.dto.GetCreateSingleCardResponse;
import kg.musabaev.em_bank_rest.dto.TransferBetweenCardsRequest;
import kg.musabaev.em_bank_rest.entity.Card;
import kg.musabaev.em_bank_rest.entity.CardBlockRequest;
import kg.musabaev.em_bank_rest.entity.CardStatus;
import kg.musabaev.em_bank_rest.entity.User;
import kg.musabaev.em_bank_rest.exception.CardNotFoundException;
import kg.musabaev.em_bank_rest.mapper.CardMapper;
import kg.musabaev.em_bank_rest.repository.CardBlockRequestRepository;
import kg.musabaev.em_bank_rest.repository.CardRepository;
import kg.musabaev.em_bank_rest.util.SomePaymentSystemProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

@Service
@RequiredArgsConstructor
public class SimpleCardService implements kg.musabaev.em_bank_rest.service.CardService {

    private final CardRepository cardRepository;
    private final CardMapper cardMapper;
    private final SimpleUserService userService;
    private final SomePaymentSystemProvider paymentSystemProvider;
    private final CardBlockRequestRepository cardBlockRequestRepository;

    @Override
    @Transactional
    public GetCreateSingleCardResponse create(Long userId) {
        var assignedUser = userService.getById(userId);

        var newCard = Card.builder()
                .number(paymentSystemProvider.generateEncryptedRandomCardNumber())
                .expiry(LocalDate.now().plusYears(3))
                .status(CardStatus.ACTIVE)
                .balance(new BigDecimal(0))
                .user(assignedUser)
                .owner(assignedUser.getFullName())
                .build();

        var persistedCard = cardRepository.save(newCard);
        return cardMapper.toCreateCardResponse(persistedCard);
    }

    @Override
    public GetCreateSingleCardResponse getById(Long id) {
        var foundCard = cardRepository.findById(id)
                .orElseThrow(() -> new CardNotFoundException(id));
        return cardMapper.toGetSingleCardResponse(foundCard);
    }

    @Override
    public Page<GetCreateSingleCardResponse> getAllCards(Specification<Card> spec, Pageable pageable) {
        var cards = cardRepository.findAll(spec, pageable);
        return cards.map(cardMapper::toGetSingleCardResponse);
    }

    @Override
    @Transactional
    public Card updateStatus(Long cardId, CardStatus newStatus) {
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new CardNotFoundException(cardId));
        card.setStatus(newStatus);
        return cardRepository.save(card);
    }

    // user
    @Override
    public Page<Card> getUserCards(User user, Pageable pageable/*, User authorizedUser*/) {
        return cardRepository.findAllByUser(user, pageable);
    }

    @Override
    public BigDecimal getCardBalance(Long cardId) {
        var card = cardRepository.findById(cardId)
                .orElseThrow(() -> new CardNotFoundException(cardId));
        return card.getBalance();
    }

    @Override
    @Transactional
    public void blockCard(Long cardId) {
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new CardNotFoundException(cardId));
        if (card.getStatus() == CardStatus.BLOCKED)
            throw new ResponseStatusException(BAD_REQUEST, "couldn't block already blocked card");

        var assignedUser = userService.getById(1L); // FIXME
        cardBlockRequestRepository.save(CardBlockRequest.builder()
                .cardToBlock(card)
                .requesterUser(assignedUser)
                .processingStatus(CardBlockRequest.Status.IN_PROGRESS)
                .build());
    }

    @Override
    public void delete(Long id) {
        cardRepository.deleteById(id);
    }

    @Override
    @Transactional
    public void transferMoney(/*FIXME*/ User user, TransferBetweenCardsRequest dto) {
        if (dto.fromCardNumber().equals(dto.toCardNumber()))
            throw new ResponseStatusException(BAD_REQUEST, "Source and destination cards must be different");

        var fromCard = cardRepository.findByNumber(paymentSystemProvider.encryptCardNumber(dto.fromCardNumber()))
                .orElseThrow(() -> new CardNotFoundException(dto.fromCardNumber()));
        var toCard = cardRepository.findByNumber(paymentSystemProvider.encryptCardNumber(dto.toCardNumber()))
                .orElseThrow(() -> new CardNotFoundException(dto.toCardNumber()));

        if (fromCard.getStatus() != CardStatus.ACTIVE)
            throw new ResponseStatusException(BAD_REQUEST, "Source card must be active");
        if (!fromCard.getUser().equals(user) || !toCard.getUser().equals(user))
            throw new ResponseStatusException(BAD_REQUEST, "Both cards must belong to one user");
        if (fromCard.getBalance().compareTo(dto.amount()) < 0) {
            throw new ResponseStatusException(BAD_REQUEST, "Insufficient funds");
        }

        fromCard.setBalance(fromCard.getBalance().subtract(dto.amount()));
        toCard.setBalance(toCard.getBalance().add(dto.amount()));

        cardRepository.save(fromCard);
        cardRepository.save(toCard);
    }
}
