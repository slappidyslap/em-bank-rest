package kg.musabaev.em_bank_rest.service;

import kg.musabaev.em_bank_rest.dto.CreateCardRequest;
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
import kg.musabaev.em_bank_rest.security.Role;
import kg.musabaev.em_bank_rest.security.SimpleUserDetails;
import kg.musabaev.em_bank_rest.service.impl.SimpleCardService;
import kg.musabaev.em_bank_rest.util.Pair;
import kg.musabaev.em_bank_rest.util.SomePaymentSystemProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SimpleCardServiceTest {

    @Mock
    private CardRepository cardRepo;
    @Mock
    private CardMapper cardMapper;
    @Mock
    private SomePaymentSystemProvider paymentSystemProvider;
    @Mock
    private CardBlockRequestRepository cardBlockRequestRepo;
    @Mock
    private UserRepository userRepo;

    @InjectMocks
    private SimpleCardService cardService;

    private User user;
    private User admin;
    private SimpleUserDetails userDetails;
    private SimpleUserDetails adminDetails;
    private Card activeCard;
    private Card blockedCard;
    private GetCreatePatchCardResponse getCreatePatchCardResponse;
    private final Long USER_ID = 1L;
    private final Long CARD_ID = 10L;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(USER_ID)
                .fullName("Test User")
                .email("test@user.com")
                .role(Role.USER)
                .build();
        admin = User.builder()
                .id(2L)
                .fullName("Test Admin")
                .email("admin@user.com")
                .role(Role.ADMIN)
                .build();
        userDetails = new SimpleUserDetails(user);
        adminDetails = new SimpleUserDetails(admin);
        activeCard = Card.builder()
                .id(CARD_ID)
                .user(user)
                .status(CardStatus.ACTIVE)
                .balance(BigDecimal.valueOf(1000.00))
                .number("ENCRYPTED_1234")
                .expiry(LocalDate.now().plusYears(3))
                .build();
        blockedCard = Card.builder()
                .id(CARD_ID)
                .user(user)
                .status(CardStatus.BLOCKED)
                .balance(BigDecimal.valueOf(100.00))
                .number("ENCRYPTED_5678")
                .expiry(LocalDate.now().plusYears(3))
                .build();
        getCreatePatchCardResponse = GetCreatePatchCardResponse.builder()
                .id(CARD_ID)
                .build();

        lenient()
                .when(cardMapper.toCreateCardResponse(any(Card.class)))
                .thenReturn(getCreatePatchCardResponse);
        lenient()
                .when(cardMapper.toGetCardResponse(any(Card.class)))
                .thenReturn(getCreatePatchCardResponse);
        lenient()
                .when(cardMapper.toPatchCardResponse(any(Card.class)))
                .thenReturn(getCreatePatchCardResponse);
    }

    @Test
    @DisplayName("Get All For Admin: Should return page of cards")
    @SuppressWarnings("unchecked")
    void getAllForAdmin_ShouldReturnPageOfCards() {
        var pageable = Pageable.ofSize(10);
        var cardList = List.of(activeCard, blockedCard);
        var cardPage = new PageImpl<>(cardList, pageable, cardList.size());
        var filters = mock(CardSpecification.class);

        when(filters.build()).thenReturn(null);
        when(cardRepo.findAll(isNull(Specification.class), eq(pageable))).thenReturn(cardPage);

        var result = cardService.getAllForAdmin(filters, pageable);

        assertThat(result.getContent()).hasSize(2);
        verify(cardRepo, times(1)).findAll(isNull(Specification.class), eq(pageable));
    }

    @Test
    @DisplayName("Get By ID For Admin: Should return card")
    void getByIdForAdmin_ShouldReturnResponse_WhenCardExists() {
        when(cardRepo.findById(CARD_ID)).thenReturn(Optional.of(activeCard));

        var result = cardService.getByIdForAdmin(CARD_ID);

        assertThat(result).isEqualTo(getCreatePatchCardResponse);
        verify(cardRepo, times(1)).findById(CARD_ID);
    }

    @Test
    @DisplayName("Get By ID For Admin: Should throw CardNotFoundException when card doesn't exits")
    void getByIdForAdmin_ShouldThrowCardNotFoundException_WhenCardDoesNotExist() {
        when(cardRepo.findById(CARD_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cardService.getByIdForAdmin(CARD_ID))
                .isInstanceOf(CardNotFoundException.class);
        verify(cardRepo, times(1)).findById(eq(CARD_ID));
    }

    @Test
    @DisplayName("Get All For User: Should return page of cards")
    void getAllForUser_ShouldReturnPageOfCards() {
        var cardList = List.of(activeCard, blockedCard);
        var pageable = Pageable.ofSize(10);
        var cardPage = new PageImpl<>(cardList, pageable, cardList.size());

        when(cardRepo.findAllByUser(user, pageable)).thenReturn(cardPage);

        var result = cardService.getAllForUser(pageable, userDetails);

        assertThat(result.getContent()).hasSize(2);
        verify(cardRepo, times(1)).findAllByUser(eq(user), eq(pageable));
    }

    @Test
    @DisplayName("Get by ID For User: Should return card details when card belongs to user")
    void getByIdForUser_ShouldReturnDetails_WhenOwned() {
        when(cardRepo.findById(CARD_ID)).thenReturn(Optional.of(activeCard));

        cardService.getByIdForUser(CARD_ID, userDetails);

        verify(cardRepo, times(1)).findById(eq(CARD_ID));
    }

    @Test
    @DisplayName("Get by ID For User: Should throw CardNotFoundException when card is not found")
    void getByIdForUser_ShouldThrowCardNotFoundException_WhenCardNotFound() {
        when(cardRepo.findById(CARD_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cardService.getByIdForUser(CARD_ID, userDetails))
                .isInstanceOf(CardNotFoundException.class);
        verify(cardRepo, times(1)).findById(eq(CARD_ID));
    }

    @Test
    @DisplayName("Get by ID For User: Should throw CardOwnershipException when card doesn't belong to user")
    void getByIdForUser_ShouldThrowOwnershipException_WhenNotOwned() {
        var anotherUser = User.builder().email("another@user.com").build();
        var card = Card.builder().id(CARD_ID).user(anotherUser).build();

        when(cardRepo.findById(CARD_ID)).thenReturn(Optional.of(card));

        assertThatThrownBy(() -> cardService.getByIdForUser(CARD_ID, userDetails))
                .isInstanceOf(CardOwnershipException.class);
        verify(cardRepo, times(1)).findById(eq(CARD_ID));
    }

    @Test
    @DisplayName("Create: Should successful create card")
    void create_ShouldCreateCard_WhenSuccessful() {
        var createRequest = new CreateCardRequest(USER_ID);
        var encryptedNumber = "1234567890123456";

        when(userRepo.findById(USER_ID)).thenReturn(Optional.of(user));
        when(paymentSystemProvider.generateEncryptedRandomCardNumber()).thenReturn(encryptedNumber);
        when(cardRepo.save(any(Card.class))).thenReturn(activeCard);

        var result = cardService.create(createRequest);

        assertThat(result.id()).isEqualTo(getCreatePatchCardResponse.id());
        verify(userRepo, times(1)).findById(USER_ID);
        verify(cardRepo, times(1)).save(any(Card.class));
    }

    @Test
    @DisplayName("Create: Should throw UserNotFoundException when user doesn't exists")
    void create_ShouldThrowUserNotFoundException_WhenUserDoesNotExist() {
        long userId = 99L;
        var createRequest = new CreateCardRequest(userId);

        when(userRepo.findById(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cardService.create(createRequest))
                .isInstanceOf(UserNotFoundException.class);
        verify(userRepo, times(1)).findById(eq(userId));
        verify(cardRepo, never()).save(any());
    }

    @Test
    @DisplayName("Get Balance: Should return balance when card belongs to user")
    void getBalance_ShouldReturnDetails_WhenOwned() {
        when(cardRepo.findById(CARD_ID)).thenReturn(Optional.of(activeCard));

        Pair<BigDecimal> response = cardService.getBalance(CARD_ID, userDetails);

        assertThat(response.key()).isEqualTo("balance");
        verify(cardRepo, times(1)).findById(eq(CARD_ID));
    }

    @Test
    @DisplayName("Get Balance: Should throw CardNotFoundException when card is not found")
    void getBalance_ShouldThrowCardNotFoundException_WhenCardNotFound() {
        when(cardRepo.findById(CARD_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cardService.getBalance(CARD_ID, userDetails))
                .isInstanceOf(CardNotFoundException.class);
        verify(cardRepo, times(1)).findById(eq(CARD_ID));
    }

    @Test
    @DisplayName("Get Balance: Should throw CardOwnershipException when card doesn't belong to user")
    void getBalance_ShouldThrowOwnershipException_WhenNotOwned() {
        var anotherUser = User.builder().email("another@user.com").build();
        var card = Card.builder().id(CARD_ID).user(anotherUser).build();

        when(cardRepo.findById(CARD_ID)).thenReturn(Optional.of(card));

        assertThatThrownBy(() -> cardService.getBalance(CARD_ID, userDetails))
                .isInstanceOf(CardOwnershipException.class);
        verify(cardRepo, times(1)).findById(eq(CARD_ID));
    }

    @Test
    @DisplayName("Patch Status: Should block card with IN_PROGRESS request")
    void patchStatusForAdmin_ShouldBlockCard() {
        var cardToBlock = Card.builder().id(CARD_ID).status(CardStatus.ACTIVE).build();
        var dto = new UpdateStatusCardRequest(CardStatus.BLOCKED);
        var cardBlockRequest = CardBlockRequest.builder()
                .processingStatus(CardBlockRequest.Status.IN_PROGRESS)
                .cardToBlock(cardToBlock)
                .build();

        when(cardRepo.findById(CARD_ID)).thenReturn(Optional.of(cardToBlock));
        when(cardBlockRequestRepo.findByCardToBlock(cardToBlock)).thenReturn(Optional.of(cardBlockRequest));
        when(cardRepo.save(cardToBlock)).thenReturn(cardToBlock);

        cardService.patchStatusForAdmin(CARD_ID, dto, adminDetails);

        assertThat(cardToBlock.getStatus()).isEqualTo(CardStatus.BLOCKED);
        assertThat(cardBlockRequest.getProcessingStatus()).isEqualTo(CardBlockRequest.Status.DONE);
        assertThat(cardBlockRequest.getProcesserUser().getId()).isEqualTo(admin.getId());
        verify(cardRepo, times(1)).save(eq(cardToBlock));
        verify(cardBlockRequestRepo, times(1)).findByCardToBlock(eq(cardToBlock));
    }

    @Test
    @DisplayName("Patch Status: Should activate card from BLOCKED status")
    void patchStatusForAdmin_ShouldActivateCard() {
        var cardToActivate = Card.builder().id(CARD_ID).status(CardStatus.BLOCKED).build();
        var dto = new UpdateStatusCardRequest(CardStatus.ACTIVE);

        when(cardRepo.findById(CARD_ID)).thenReturn(Optional.of(cardToActivate));
        when(cardRepo.save(cardToActivate)).thenReturn(cardToActivate);

        cardService.patchStatusForAdmin(CARD_ID, dto, adminDetails);

        assertThat(cardToActivate.getStatus()).isEqualTo(CardStatus.ACTIVE);
        verify(cardRepo, times(1)).save(eq(cardToActivate));
    }

    @Test
    @DisplayName("Patch Status: Should throw CardExpiredException when card already expired")
    void patchStatusForAdmin_ShouldThrowCardExpiredException() {
        var expiredCard = Card.builder().id(CARD_ID).status(CardStatus.EXPIRED).build();
        var dto = new UpdateStatusCardRequest(CardStatus.ACTIVE);

        when(cardRepo.findById(CARD_ID)).thenReturn(Optional.of(expiredCard));

        assertThatThrownBy(() -> cardService.patchStatusForAdmin(CARD_ID, dto, adminDetails))
                .isInstanceOf(CardExpiredException.class);
        verify(cardRepo, never()).save(any());
        verify(cardBlockRequestRepo, never()).findByCardToBlock(any());
    }

    @Test
    @DisplayName("Patch Status: Should throw CardUnsupportedOperationException when update status card request is EXPIRED")
    void patchStatusForAdmin_ShouldThrowCardUnsupportedOperationException() {
        var card = Card.builder().id(CARD_ID).status(CardStatus.ACTIVE).build();
        var dto = new UpdateStatusCardRequest(CardStatus.EXPIRED);

        when(cardRepo.findById(CARD_ID)).thenReturn(Optional.of(card));

        assertThatThrownBy(() -> cardService.patchStatusForAdmin(CARD_ID, dto, adminDetails))
                .isInstanceOf(CardUnsupportedOperationException.class);
        verify(cardRepo, never()).save(any());
        verify(cardBlockRequestRepo, never()).findByCardToBlock(any());
    }

    @Test
    @DisplayName("Patch Status: Should throw CardUnsupportedOperationException when block request is already DONE")
    void patchStatusForAdmin_ShouldThrowWhenBlockRequestAlreadyDone() {
        var card = Card.builder().id(CARD_ID).status(CardStatus.BLOCKED).build();
        var cardBlockRequest = CardBlockRequest.builder()
                .processingStatus(CardBlockRequest.Status.DONE)
                .cardToBlock(card)
                .build();
        var dto = new UpdateStatusCardRequest(CardStatus.BLOCKED);

        when(cardRepo.findById(CARD_ID)).thenReturn(Optional.of(card));
        when(cardBlockRequestRepo.findByCardToBlock(card)).thenReturn(Optional.of(cardBlockRequest));

        assertThatThrownBy(() -> cardService.patchStatusForAdmin(CARD_ID, dto, adminDetails))
                .isInstanceOf(CardUnsupportedOperationException.class);
        verify(cardRepo, never()).save(any());
        verify(cardBlockRequestRepo, times(1)).findByCardToBlock(any());
    }

    @Test
    @DisplayName("Patch Status: Should throw CardUnsupportedOperationException when activating an already ACTIVE card")
    void patchStatusForAdmin_ShouldThrowWhenCardAlreadyActive() {
        var card = Card.builder().id(CARD_ID).status(CardStatus.ACTIVE).build();
        var dto = new UpdateStatusCardRequest(CardStatus.ACTIVE);

        when(cardRepo.findById(CARD_ID)).thenReturn(Optional.of(card));

        assertThatThrownBy(() -> cardService.patchStatusForAdmin(CARD_ID, dto, adminDetails))
                .isInstanceOf(CardUnsupportedOperationException.class);

        verify(cardRepo, never()).save(any());
        verify(cardBlockRequestRepo, never()).findByCardToBlock(any());
    }

    @Test
    @DisplayName("Patch Status: Should throw CardBlockRequestNotFoundException when blocking without card block request")
    void patchStatusForAdmin_ShouldThrowCardBlockRequestNotFoundException() {
        var card = Card.builder().id(CARD_ID).status(CardStatus.ACTIVE).build();
        var dto = new UpdateStatusCardRequest(CardStatus.BLOCKED);

        when(cardRepo.findById(CARD_ID)).thenReturn(Optional.of(card));
        when(cardBlockRequestRepo.findByCardToBlock(card)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cardService.patchStatusForAdmin(CARD_ID, dto, adminDetails))
                .isInstanceOf(CardBlockRequestNotFoundException.class);

        verify(cardRepo, never()).save(any());
        verify(cardBlockRequestRepo, times(1)).findByCardToBlock(any());
    }

    @Test
    @DisplayName("Patch Status: Should throw CardNotFoundException when card is not found")
    void patchStatusForAdmin_ShouldThrowCardNotFoundException() {
        var dto = new UpdateStatusCardRequest(CardStatus.ACTIVE);

        when(cardRepo.findById(CARD_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cardService.patchStatusForAdmin(CARD_ID, dto, adminDetails))
                .isInstanceOf(CardNotFoundException.class);

        verify(cardRepo, never()).save(any());
        verify(cardBlockRequestRepo, never()).findByCardToBlock(any());
    }

    @Test
    @DisplayName("Request Block: Should create card block request when card is active")
    void requestBlockCard_ShouldCreateRequest() {
        when(cardRepo.findById(CARD_ID)).thenReturn(Optional.of(activeCard));

        cardService.requestBlockCard(CARD_ID, userDetails);

        verify(cardBlockRequestRepo, times(1)).save(any(CardBlockRequest.class));
        verify(cardRepo, times(1)).findById(CARD_ID);
    }

    @Test
    @DisplayName("Request Block: Should throw CardUnsupportedOperationException when card already blocked")
    void requestBlockCard_ShouldThrowWhenCardAlreadyBlocked() {
        when(cardRepo.findById(CARD_ID)).thenReturn(Optional.of(blockedCard));

        assertThatThrownBy(() -> cardService.requestBlockCard(CARD_ID, userDetails))
                .isInstanceOf(CardUnsupportedOperationException.class);

        verify(cardBlockRequestRepo, never()).save(any());
    }

    @Test
    @DisplayName("Request Block: Should throw CardOwnershipException when card does not belong to user")
    void requestBlockCard_ShouldThrowWhenCardDoesNotBelongToUser() {
        var anotherUser = User.builder().email("another@user.com").build();
        var card = Card.builder().id(CARD_ID).user(anotherUser).status(CardStatus.ACTIVE).build();

        when(cardRepo.findById(CARD_ID)).thenReturn(Optional.of(card));

        assertThatThrownBy(() -> cardService.requestBlockCard(CARD_ID, userDetails))
                .isInstanceOf(CardOwnershipException.class);

        verify(cardBlockRequestRepo, never()).save(any());
    }

    @Test
    @DisplayName("Delete: Should delete if card exists")
    void delete_ShouldCallDelete_WhenCardExists() {
        when(cardRepo.existsById(CARD_ID)).thenReturn(true);

        cardService.delete(CARD_ID);

        verify(cardRepo, times(1)).deleteById(eq(CARD_ID));
    }

    @Test
    @DisplayName("Delete: Should throw CardNotFoundException when card does not exist")
    void delete_ShouldThrowCardNotFoundException_WhenCardDoesNotExist() {
        when(cardRepo.existsById(CARD_ID)).thenReturn(false);

        assertThatThrownBy(() -> cardService.delete(CARD_ID))
                .isInstanceOf(CardNotFoundException.class);
        verify(cardRepo, never()).deleteById(any());
    }

    @Test
    @DisplayName("Transfer: Should transfer money")
    void transferMoney_ShouldUpdateBalances_WhenSuccessful() {
        var initFromBalance = BigDecimal.valueOf(500);
        var initToBalance = BigDecimal.valueOf(1000);
        var transferAmount = BigDecimal.valueOf(100);
        var expectedFromBalance = BigDecimal.valueOf(400);
        var expectedToBalance = BigDecimal.valueOf(1100);
        var fromEncryptedNumber = "ENC_FROM";
        var toEncryptedNumber = "ENC_TO";
        var fromNumber = "FROM";
        var toNumber = "TO";


        var fromCard = Card.builder()
                .balance(initFromBalance)
                .number(fromEncryptedNumber)
                .status(CardStatus.ACTIVE)
                .user(user)
                .build();
        var toCard = Card.builder()
                .balance(initToBalance)
                .number(toEncryptedNumber)
                .status(CardStatus.ACTIVE)
                .user(user)
                .build();
        var dto = TransferBetweenCardsRequest.builder()
                .toCardNumber(toNumber)
                .fromCardNumber(fromNumber)
                .amount(transferAmount)
                .build();

        // FROM --> ENC_FROM
        when(paymentSystemProvider.encryptCardNumber(fromNumber)).thenReturn(fromEncryptedNumber);
        when(paymentSystemProvider.encryptCardNumber(toNumber)).thenReturn(toEncryptedNumber);
        when(cardRepo.findByNumber(fromEncryptedNumber)).thenReturn(Optional.of(fromCard));
        when(cardRepo.findByNumber(toEncryptedNumber)).thenReturn(Optional.of(toCard));

        cardService.transferMoney(userDetails, dto);

        assertThat(fromCard.getBalance()).isEqualTo(expectedFromBalance);
        assertThat(toCard.getBalance()).isEqualTo(expectedToBalance);
        verify(cardRepo, times(1)).save(eq(fromCard));
        verify(cardRepo, times(1)).save(eq(toCard));
    }

    @Test
    @DisplayName("Transfer: Should throw SelfTransferNotAllowedException when source and destination cards are the same")
    void transferMoney_ShouldThrowSelfTransferNotAllowedException() {
        var sameCardNumber = "1111";
        var request = TransferBetweenCardsRequest.builder()
                .fromCardNumber(sameCardNumber)
                .toCardNumber(sameCardNumber)
                .amount(BigDecimal.TEN)
                .build();

        assertThatThrownBy(() -> cardService.transferMoney(userDetails, request))
                .isInstanceOf(SelfTransferNotAllowedException.class);
        verify(cardRepo, never()).findByNumber(any());
        verify(cardRepo, never()).save(any());
    }

    @Test
    @DisplayName("Transfer: Should throw CardNotFoundException when 'from' card is not found")
    void transferMoney_ShouldThrowCardNotFoundException_WhenFromCardNotFound() {
        var request = TransferBetweenCardsRequest.builder()
                .fromCardNumber("9999")
                .toCardNumber("1111")
                .amount(BigDecimal.TEN)
                .build();

        when(paymentSystemProvider.encryptCardNumber(request.fromCardNumber())).thenReturn("ENC_FROM_NOT_FOUND");

        when(cardRepo.findByNumber("ENC_FROM_NOT_FOUND")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cardService.transferMoney(userDetails, request))
                .isInstanceOf(CardNotFoundException.class);
        verify(cardRepo, never()).save(any());
    }

    @Test
    @DisplayName("Transfer: Should throw CardNotFoundException when 'to' card is not found")
    void transferMoney_ShouldThrowCardNotFoundException_WhenToCardNotFound() {
        var request = TransferBetweenCardsRequest.builder()
                .fromCardNumber("1111")
                .toCardNumber("9999")
                .amount(BigDecimal.TEN)
                .build();

        when(paymentSystemProvider.encryptCardNumber(request.fromCardNumber())).thenReturn("ENC_FROM");
        when(paymentSystemProvider.encryptCardNumber(request.toCardNumber())).thenReturn("ENC_TO_NOT_FOUND");

        when(cardRepo.findByNumber("ENC_FROM")).thenReturn(Optional.of(activeCard));
        when(cardRepo.findByNumber("ENC_TO_NOT_FOUND")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cardService.transferMoney(userDetails, request))
                .isInstanceOf(CardNotFoundException.class);
        verify(cardRepo, never()).save(any());
    }

    @Test
    @DisplayName("Transfer: Should throw InactiveCardException when 'from' card is BLOCKED")
    void transferMoney_ShouldThrowInactiveCardException_WhenFromCardBlocked() {
        var fromEncryptedNumber = "ENC_FROM";
        var toEncryptedNumber = "ENC_TO";
        var fromNumber = "1111";
        var toNumber = "2222";
        var fromCardBlocked = Card.builder().status(CardStatus.BLOCKED).build();
        var toCard = Card.builder().status(CardStatus.ACTIVE).build();
        var request = TransferBetweenCardsRequest.builder()
                .fromCardNumber(fromNumber)
                .toCardNumber(toNumber)
                .amount(BigDecimal.TEN)
                .build();

        when(paymentSystemProvider.encryptCardNumber(fromNumber)).thenReturn(fromEncryptedNumber);
        when(paymentSystemProvider.encryptCardNumber(toNumber)).thenReturn(toEncryptedNumber);

        when(cardRepo.findByNumber(fromEncryptedNumber)).thenReturn(Optional.of(fromCardBlocked));
        when(cardRepo.findByNumber(toEncryptedNumber)).thenReturn(Optional.of(toCard));

        assertThatThrownBy(() -> cardService.transferMoney(userDetails, request))
                .isInstanceOf(InactiveCardException.class);
        verify(cardRepo, never()).save(any());
    }

    @Test
    @DisplayName("Transfer: Should throw InactiveCardException when 'to' card is BLOCKED")
    void transferMoney_ShouldThrowInactiveCardException_WhenToCardBlocked() {
        var fromEncryptedNumber = "ENC_FROM";
        var toEncryptedNumber = "ENC_TO";
        var fromNumber = "1111";
        var toNumber = "2222";
        var fromCardBlocked = Card.builder().status(CardStatus.ACTIVE).build();
        var toCard = Card.builder().status(CardStatus.BLOCKED).build();
        var request = TransferBetweenCardsRequest.builder()
                .fromCardNumber(fromNumber)
                .toCardNumber(toNumber)
                .amount(BigDecimal.TEN)
                .build();

        when(paymentSystemProvider.encryptCardNumber(fromNumber)).thenReturn(fromEncryptedNumber);
        when(paymentSystemProvider.encryptCardNumber(toNumber)).thenReturn(toEncryptedNumber);

        when(cardRepo.findByNumber(fromEncryptedNumber)).thenReturn(Optional.of(fromCardBlocked));
        when(cardRepo.findByNumber(toEncryptedNumber)).thenReturn(Optional.of(toCard));

        assertThatThrownBy(() -> cardService.transferMoney(userDetails, request))
                .isInstanceOf(InactiveCardException.class);
        verify(cardRepo, never()).save(any());
    }

    @Test
    @DisplayName("Transfer: Should throw InsufficientFundsException when 'from' card balance is too low")
    void transferMoney_ShouldThrowInsufficientFundsException() {
        var fromEncryptedNumber = "ENC_FROM";
        var toEncryptedNumber = "ENC_TO";
        var fromNumber = "111";
        var toNumber = "2222";
        var fromCard = Card.builder()
                .status(CardStatus.ACTIVE)
                .user(user)
                .balance(BigDecimal.valueOf(50))
                .build();
        var toCard = Card.builder()
                .status(CardStatus.ACTIVE)
                .user(user)
                .balance(BigDecimal.valueOf(1000))
                .build();
        var request = TransferBetweenCardsRequest.builder()
                .fromCardNumber(fromNumber)
                .toCardNumber(toNumber)
                .amount(BigDecimal.valueOf(100))
                .build();

        when(paymentSystemProvider.encryptCardNumber(fromNumber)).thenReturn(fromEncryptedNumber);
        when(paymentSystemProvider.encryptCardNumber(toNumber)).thenReturn(toEncryptedNumber);

        when(cardRepo.findByNumber(fromEncryptedNumber)).thenReturn(Optional.of(fromCard));
        when(cardRepo.findByNumber(toEncryptedNumber)).thenReturn(Optional.of(toCard));

        assertThatThrownBy(() -> cardService.transferMoney(userDetails, request))
                .isInstanceOf(InsufficientFundsException.class);
        verify(cardRepo, never()).save(any());
    }

    @Test
    @DisplayName("Transfer: Should throw CardOwnershipException when 'from' card does not belong to authenticated user")
    void transferMoney_ShouldThrowCardOwnershipException_WhenFromCardNotOwned() {
        var fromEncryptedNumber = "ENC_FROM";
        var toEncryptedNumber = "ENC_TO";
        var fromNumber = "1111";
        var toNumber = "2222";
        var fromCard = Card.builder()
                .user(admin)
                .status(CardStatus.ACTIVE)
                .balance(BigDecimal.valueOf(1000))
                .build();
        var toCardOwned = Card.builder()
                .user(user)
                .status(CardStatus.ACTIVE)
                .balance(BigDecimal.valueOf(1000))
                .build();
        var request = TransferBetweenCardsRequest.builder()
                .fromCardNumber(toNumber)
                .toCardNumber(fromNumber)
                .amount(BigDecimal.TEN)
                .build();

        when(paymentSystemProvider.encryptCardNumber(fromNumber)).thenReturn(fromEncryptedNumber);
        when(paymentSystemProvider.encryptCardNumber(toNumber)).thenReturn(toEncryptedNumber);

        when(cardRepo.findByNumber(fromEncryptedNumber)).thenReturn(Optional.of(toCardOwned));
        when(cardRepo.findByNumber(toEncryptedNumber)).thenReturn(Optional.of(fromCard));

        assertThatThrownBy(() -> cardService.transferMoney(userDetails, request))
                .isInstanceOf(CardOwnershipException.class);
        verify(cardRepo, never()).save(any());
    }
    @Test
    @DisplayName("Transfer: Should throw CardOwnershipException when 'to' card does not belong to authenticated user")
    void transferMoney_ShouldThrowCardOwnershipException_WhenToCardNotOwned() {
        var fromEncryptedNumber = "ENC_FROM";
        var toEncryptedNumber = "ENC_TO";
        var fromNumber = "1111";
        var toNumber = "2222";
        var fromCard = Card.builder()
                .user(user)
                .status(CardStatus.ACTIVE)
                .balance(BigDecimal.valueOf(1000))
                .build();
        var toCardOwned = Card.builder()
                .user(admin)
                .status(CardStatus.ACTIVE)
                .balance(BigDecimal.valueOf(1000))
                .build();
        var request = TransferBetweenCardsRequest.builder()
                .fromCardNumber(toNumber)
                .toCardNumber(fromNumber)
                .amount(BigDecimal.TEN)
                .build();

        when(paymentSystemProvider.encryptCardNumber(fromNumber)).thenReturn(fromEncryptedNumber);
        when(paymentSystemProvider.encryptCardNumber(toNumber)).thenReturn(toEncryptedNumber);

        when(cardRepo.findByNumber(fromEncryptedNumber)).thenReturn(Optional.of(toCardOwned));
        when(cardRepo.findByNumber(toEncryptedNumber)).thenReturn(Optional.of(fromCard));

        assertThatThrownBy(() -> cardService.transferMoney(userDetails, request))
                .isInstanceOf(CardOwnershipException.class);
        verify(cardRepo, never()).save(any());
    }
}
