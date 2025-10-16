package kg.musabaev.em_bank_rest.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import kg.musabaev.em_bank_rest.dto.GetCreatePatchCardResponse;
import kg.musabaev.em_bank_rest.dto.TransferBetweenCardsRequest;
import kg.musabaev.em_bank_rest.dto.UpdateStatusCardRequest;
import kg.musabaev.em_bank_rest.entity.Card;
import kg.musabaev.em_bank_rest.entity.CardBlockRequest;
import kg.musabaev.em_bank_rest.entity.CardStatus;
import kg.musabaev.em_bank_rest.entity.User;
import kg.musabaev.em_bank_rest.exception.*;
import kg.musabaev.em_bank_rest.mapper.CardMapper;
import kg.musabaev.em_bank_rest.repository.CardBlockRequestRepository;
import kg.musabaev.em_bank_rest.repository.CardRepository;
import kg.musabaev.em_bank_rest.repository.UserRepository;
import kg.musabaev.em_bank_rest.util.SomePaymentSystemProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class SimpleCardService implements kg.musabaev.em_bank_rest.service.CardService {

    private final CardRepository cardRepository;
    private final CardMapper cardMapper;
    private final SomePaymentSystemProvider paymentSystemProvider;
    private final CardBlockRequestRepository cardBlockRequestRepository;
    private final UserRepository userRepository;

    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public GetCreatePatchCardResponse create(Long userId) {
        var assignedUser = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

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
    @Transactional(readOnly = true)
    public GetCreatePatchCardResponse getById(Long id) {
        var foundCard = cardRepository.findById(id)
                .orElseThrow(() -> new CardNotFoundException(id));
        return cardMapper.toGetCardResponse(foundCard);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<GetCreatePatchCardResponse> getAllCards(Specification<Card> spec, Pageable pageable) {
        var cards = cardRepository.findAll(spec, pageable);
        return cards.map(cardMapper::toGetCardResponse);
    }

    @Override
    @Transactional
    public GetCreatePatchCardResponse patchStatus(Long id, UpdateStatusCardRequest dto) {
        Card card = cardRepository.findById(id)
                .orElseThrow(() -> new CardNotFoundException(id));
        card.setStatus(dto.status());
        return cardMapper.toPatchCardResponse(cardRepository.save(card));
    }

    // user
    @Override
    @Transactional(readOnly = true)
    public Page<Card> getUserCards(User user, Pageable pageable/*, User authorizedUser*/) {
        return cardRepository.findAllByUser(user, pageable);
    }

    @Override
    @Transactional(readOnly = true)
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
            throw new CardAlreadyBlockedException();

        var assignedUser = userRepository.findById(card.getUser().getId())
                .orElseThrow(() -> new UserNotFoundException(card.getUser().getId()));
        cardBlockRequestRepository.save(CardBlockRequest.builder()
                .cardToBlock(card)
                .requesterUser(assignedUser)
                .processingStatus(CardBlockRequest.Status.IN_PROGRESS)
                .build());
    }

    @Override
    @Transactional
    public void delete(Long id) {
        cardRepository.deleteById(id);
    }

    @Override
    @Transactional
    public void transferMoney(/*FIXME*/ User user, TransferBetweenCardsRequest dto) {
        if (dto.fromCardNumber().equals(dto.toCardNumber()))
            throw new SelfTransferNotAllowedException();

        var fromCard = cardRepository.findByNumber(paymentSystemProvider.encryptCardNumber(dto.fromCardNumber()))
                .orElseThrow(() -> new CardNotFoundException(dto.fromCardNumber()));
        var toCard = cardRepository.findByNumber(paymentSystemProvider.encryptCardNumber(dto.toCardNumber()))
                .orElseThrow(() -> new CardNotFoundException(dto.toCardNumber()));

        if (fromCard.getStatus() != CardStatus.ACTIVE)
            throw new InactiveCardException();
        if (!fromCard.getUser().equals(user) || !toCard.getUser().equals(user))
            throw new CardOwnershipException();
        if (fromCard.getBalance().compareTo(dto.amount()) < 0) {
            throw new InsufficientFundsException();
        }

        fromCard.setBalance(fromCard.getBalance().subtract(dto.amount()));
        toCard.setBalance(toCard.getBalance().add(dto.amount()));

        cardRepository.save(fromCard);
        cardRepository.save(toCard);
    }
}
