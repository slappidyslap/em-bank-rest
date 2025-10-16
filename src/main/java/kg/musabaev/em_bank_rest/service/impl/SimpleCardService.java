package kg.musabaev.em_bank_rest.service.impl;

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
import kg.musabaev.em_bank_rest.repository.specification.CardSpecification;
import kg.musabaev.em_bank_rest.security.SimpleUserDetails;
import kg.musabaev.em_bank_rest.service.CardService;
import kg.musabaev.em_bank_rest.util.Pair;
import kg.musabaev.em_bank_rest.util.SomePaymentSystemProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class SimpleCardService implements CardService {

    private final CardRepository cardRepository;
    private final CardMapper cardMapper;
    private final SomePaymentSystemProvider paymentSystemProvider;
    private final CardBlockRequestRepository cardBlockRequestRepository;
    private final UserRepository userRepository;

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
    public Page<GetCreatePatchCardResponse> getAll(CardSpecification filters, Pageable pageable) {
        var cards = cardRepository.findAll(filters.build(), pageable);
        return cards.map(cardMapper::toGetCardResponse);
    }

    // fixme надо удостовериться что заявка
    //  на блокировку карты была (другая таблица - другая "подсистема)
    @Override
    @Transactional
    public GetCreatePatchCardResponse patchStatus(Long id, UpdateStatusCardRequest dto) {
        Card card = cardRepository.findById(id)
                .orElseThrow(() -> new CardNotFoundException(id));
        card.setStatus(dto.status());
        return cardMapper.toPatchCardResponse(cardRepository.save(card));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<GetCreatePatchCardResponse> getAll(
            CardSpecification filters,
            Pageable pageable,
            Authentication auth) {
        var authUser = getCurrentAuthenticatedUser(auth);
        var cards = cardRepository.findAllByUser(authUser, filters.build(), pageable);
        return cards.map(cardMapper::toGetCardResponse);
    }

    @Override
    @Transactional
    public void requestBlockCard(Long cardId, Authentication auth) {
        requireCardBelongUser(cardId, auth);

        var authUser = getCurrentAuthenticatedUser(auth);
        var card = cardRepository.findById(cardId).get();
        if (card.getStatus() == CardStatus.BLOCKED)
            throw new CardAlreadyBlockedException();

        cardBlockRequestRepository.save(CardBlockRequest.builder()
                .cardToBlock(card)
                .requesterUser(authUser)
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
    public void transferMoney(Authentication auth, TransferBetweenCardsRequest dto) {
        if (dto.fromCardNumber().equals(dto.toCardNumber()))
            throw new SelfTransferNotAllowedException();

        var authUser = getCurrentAuthenticatedUser(auth);
        var fromCard = cardRepository.findByNumber(paymentSystemProvider.encryptCardNumber(dto.fromCardNumber()))
                .orElseThrow(() -> new CardNotFoundException(dto.fromCardNumber()));
        var toCard = cardRepository.findByNumber(paymentSystemProvider.encryptCardNumber(dto.toCardNumber()))
                .orElseThrow(() -> new CardNotFoundException(dto.toCardNumber()));

        if (fromCard.getStatus() != CardStatus.ACTIVE)
            throw new InactiveCardException();
        if (!fromCard.getUser().equals(authUser) || !toCard.getUser().equals(authUser))
            throw new CardOwnershipException();
        if (fromCard.getBalance().compareTo(dto.amount()) < 0) {
            throw new InsufficientFundsException();
        }

        fromCard.setBalance(fromCard.getBalance().subtract(dto.amount()));
        toCard.setBalance(toCard.getBalance().add(dto.amount()));

        cardRepository.save(fromCard);
        cardRepository.save(toCard);
    }

    @Override
    @Transactional(readOnly = true)
    public GetCreatePatchCardResponse getById(Long cardId, Authentication auth) {
        requireCardBelongUser(cardId, auth);
        return cardMapper.toGetCardResponse(cardRepository.findById(cardId).get());
    }

    @Override
    public Pair<BigDecimal> getBalance(Long cardId, Authentication auth) {
        requireCardBelongUser(cardId, auth);
        return Pair.of("balance", cardRepository.findById(cardId).get().getBalance());
    }

    private void requireCardBelongUser(Long cardId, Authentication auth) {
        var authUser = getCurrentAuthenticatedUser(auth);
        var card = cardRepository.findById(cardId)
                .orElseThrow(() -> new CardNotFoundException(cardId));
        if (!card.getUser().equals(authUser))
            throw new CardOwnerAuthUserMismatchException();
    }

    private User getCurrentAuthenticatedUser(Authentication auth) {
        var userDetails = (SimpleUserDetails) auth.getPrincipal();
        return userDetails.getUser();
    }
}
