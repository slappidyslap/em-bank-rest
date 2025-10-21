package kg.musabaev.em_bank_rest.service.impl;

import jakarta.validation.Valid;
import kg.musabaev.em_bank_rest.dto.*;
import kg.musabaev.em_bank_rest.entity.Card;
import kg.musabaev.em_bank_rest.entity.CardBlockRequest;
import kg.musabaev.em_bank_rest.entity.CardStatus;
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
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedModel;
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
    public GetCreatePatchCardResponse create(@Valid CreateCardRequest dto) {
        var userId = dto.userId();
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
    public PagedModel<GetCreatePatchCardResponse> getAll(CardSpecification filters, Pageable pageable) {
        var cards = cardRepository.findAll(filters.build(), pageable);
        return new PagedModel<>(cards.map(cardMapper::toGetCardResponse));
    }

    @Override
    @Transactional
    public GetCreatePatchCardResponse patchStatus(Long id, UpdateStatusCardRequest dto, SimpleUserDetails userDetails) {
        var card = cardRepository.findById(id)
                .orElseThrow(() -> new CardNotFoundException(id));
        return switch (dto.status()) {
            case BLOCKED -> {
                var request = cardBlockRequestRepository.findByCardToBlock(card)
                        .orElseThrow(() -> new CardBlockRequestNotFoundException(id));
                var actualProcessingStatus = request.getProcessingStatus();
                var admin = userDetails.getUser();

                if (actualProcessingStatus.equals(CardBlockRequest.Status.DONE))
                    throw new CardUnsupportedOperationException("Given card already blocked");

                card.setStatus(CardStatus.BLOCKED);
                request.setProcessingStatus(CardBlockRequest.Status.DONE);
                request.setProcesserUser(admin);
                yield cardMapper.toPatchCardResponse(cardRepository.save(card));
            }
            case ACTIVE -> {
                if (card.getStatus().equals(CardStatus.ACTIVE))
                    throw new CardUnsupportedOperationException("Given card already active");
                card.setStatus(CardStatus.ACTIVE);
                yield cardMapper.toPatchCardResponse(cardRepository.save(card));
            }
            case EXPIRED -> throw new CardExpiredException();
        };
    }

    @Override
    @Transactional(readOnly = true)
    public PagedModel<GetCreatePatchCardResponse> getAll(Pageable pageable, SimpleUserDetails userDetails) {
        var authUser = userDetails.getUser();
        var cards = cardRepository.findAllByUser(authUser, pageable);
        return new PagedModel<>(cards.map(cardMapper::toGetCardResponse));
    }

    @Override
    @Transactional
    public void requestBlockCard(Long cardId, SimpleUserDetails userDetails) {
        requireCardBelongUser(cardId, userDetails);

        var authUser = userDetails.getUser();
        var card = cardRepository.findById(cardId).get();
        if (card.getStatus() == CardStatus.BLOCKED)
            throw new CardUnsupportedOperationException("Given card already blocked");

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
    public void transferMoney(SimpleUserDetails userDetails, TransferBetweenCardsRequest dto) {
        if (dto.fromCardNumber().equals(dto.toCardNumber()))
            throw new SelfTransferNotAllowedException();

        var authUser = userDetails.getUser();
        var fromCard = cardRepository.findByNumber(paymentSystemProvider.encryptCardNumber(dto.fromCardNumber()))
                .orElseThrow(() -> new CardNotFoundException("fromCardNumber"));
        var toCard = cardRepository.findByNumber(paymentSystemProvider.encryptCardNumber(dto.toCardNumber()))
                .orElseThrow(() -> new CardNotFoundException("toCardNumber"));

        if (fromCard.getStatus() != CardStatus.ACTIVE)
            throw new InactiveCardException();
        if (!fromCard.getUser().equals(authUser) || !toCard.getUser().equals(authUser))
            throw new CardOwnershipException("Both cards must belong to one user");
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
    public GetCardDetailsResponse getById(Long cardId, SimpleUserDetails userDetails) {
        requireCardBelongUser(cardId, userDetails);
        Card card = cardRepository.findById(cardId).get();
        card.setNumber(paymentSystemProvider.decryptCardNumber(card.getNumber()));
        return cardMapper.toGetCardDetailsResponse(card);
    }

    @Override
    @Transactional(readOnly = true)
    public Pair<BigDecimal> getBalance(Long cardId, SimpleUserDetails userDetails) {
        requireCardBelongUser(cardId, userDetails);
        return Pair.of("balance", cardRepository.findById(cardId).get().getBalance());
    }

    private void requireCardBelongUser(Long cardId, SimpleUserDetails userDetails) {
        var authUser = userDetails.getUser();
        var card = cardRepository.findById(cardId)
                .orElseThrow(() -> new CardNotFoundException(cardId));
        if (!card.getUser().equals(authUser))
            throw new CardOwnershipException("Card by " + cardId + " does not belong to authorized user");
    }
}
