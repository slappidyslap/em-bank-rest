package kg.musabaev.em_bank_rest.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import kg.musabaev.em_bank_rest.dto.GetCardDetailsResponse;
import kg.musabaev.em_bank_rest.dto.GetCreatePatchCardResponse;
import kg.musabaev.em_bank_rest.dto.TransferBetweenCardsRequest;
import kg.musabaev.em_bank_rest.entity.CardStatus;
import kg.musabaev.em_bank_rest.entity.User;
import kg.musabaev.em_bank_rest.exception.*;
import kg.musabaev.em_bank_rest.security.JwtAuthFilter;
import kg.musabaev.em_bank_rest.security.Role;
import kg.musabaev.em_bank_rest.security.SimpleUserDetails;
import kg.musabaev.em_bank_rest.service.CardService;
import kg.musabaev.em_bank_rest.util.Pair;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedModel;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Stream;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        value = UserCardController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                value = {JwtAuthFilter.class}))
@WithMockUser(roles = {"USER"})
class UserCardControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CardService cardService;

    private final String BASE_URL = "/api/v1/users/me/cards";
    private SimpleUserDetails userDetails;
    private TransferBetweenCardsRequest transferBetweenCardsRequest;

    @BeforeEach
    void setUp() {
        userDetails = new SimpleUserDetails(User.builder()
                .id(1L)
                .role(Role.USER)
                .build());
        transferBetweenCardsRequest = TransferBetweenCardsRequest
                .builder()
                .fromCardNumber("1234123412341234")
                .toCardNumber("1234123412341235")
                .amount(BigDecimal.valueOf(100.00))
                .build();
    }

    @Test
    void getMyAllCards_ShouldReturn200_WithPagedData() throws Exception {
        var mockGetCreatePatchCardResponse = GetCreatePatchCardResponse.builder()
                .id(1L)
                .owner("TEST USER")
                .expiry(LocalDate.now().plusYears(1))
                .build();
        var pagedModel = new PagedModel<>(new PageImpl<>(List.of(mockGetCreatePatchCardResponse)));

        when(cardService.getAllForUser(any(Pageable.class), eq(userDetails))).thenReturn(pagedModel);

        mockMvc.perform(get(BASE_URL)
                        .with(user(userDetails)))
                .andExpect(status().isOk());

        verify(cardService, times(1)).getAllForUser(any(Pageable.class), eq(userDetails));
    }

    @Test
    void getCardById_ShouldReturn200_WhenCardExists() throws Exception {
        long cardId = 1L;
        var mockDetailsResponse = GetCardDetailsResponse.builder()
                .id(cardId)
                .number("1234123412341234")
                .expiry(LocalDate.now().plusYears(3))
                .owner("MUSABAEV ELDIYAR")
                .status(CardStatus.ACTIVE)
                .build();
        when(cardService.getByIdForUser(eq(cardId), eq(userDetails))).thenReturn(mockDetailsResponse);

        mockMvc.perform(get(BASE_URL + "/{cardId}", cardId)
                        .with(user(userDetails)))
                .andExpect(status().isOk());

        verify(cardService, times(1)).getByIdForUser(eq(cardId), eq(userDetails));
    }

    @Test
    void getBalance_ShouldReturn200_WhenCardExists() throws Exception {
        long cardId = 1L;
        var balanceResponse = new Pair<>("balance", BigDecimal.TEN);
        when(cardService.getBalance(eq(cardId), eq(userDetails))).thenReturn(balanceResponse);

        mockMvc.perform(get(BASE_URL + "/{cardId}/balance", cardId)
                        .with(user(userDetails)))
                .andExpect(status().isOk());

        verify(cardService, times(1)).getBalance(eq(cardId), eq(userDetails));
    }

    private static Stream<Arguments> provideExceptionsForGetCardAndGetBalance() {
        return Stream.of(
                Arguments.of(new CardNotFoundException(99L), 404),
                Arguments.of(new CardOwnershipException(""), 400)
        );
    }

    @ParameterizedTest
    @MethodSource("provideExceptionsForGetCardAndGetBalance")
    void getCardAccess_ShouldReturnCorrectStatus_WhenThrowException(AbstractHttpStatusException ex, int statusCode) throws Exception {
        long cardId = 99L;
        when(cardService.getByIdForUser(eq(cardId), any())).thenThrow(ex);
        when(cardService.getBalance(eq(cardId), any())).thenThrow(ex);

        mockMvc.perform(get(BASE_URL + "/{cardId}", cardId)
                        .with(user(userDetails)))
                .andExpect(status().is(statusCode));

        mockMvc.perform(get(BASE_URL + "/{cardId}/balance", cardId)
                        .with(user(userDetails)))
                .andExpect(status().is(statusCode));

        verify(cardService, times(1)).getByIdForUser(eq(cardId), any());
        verify(cardService, times(1)).getBalance(eq(cardId), any());
    }

    @Test
    void requestBlock_ShouldReturn204_WhenSuccessful() throws Exception {
        long cardId = 1L;
        doNothing().when(cardService).requestBlockCard(eq(cardId), eq(userDetails));

        mockMvc.perform(post(BASE_URL + "/{cardId}/request-block", cardId)
                        .with(user(userDetails))
                        .with(csrf()))
                .andExpect(status().isNoContent());

        verify(cardService, times(1)).requestBlockCard(eq(cardId), eq(userDetails));
    }

    @Test
    void requestBlock_ShouldReturnCorrectStatus_WhenThrowException() throws Exception {
        long cardId = 99L;
        doThrow(new CardUnsupportedOperationException(""))
                .when(cardService)
                .requestBlockCard(eq(cardId), eq(userDetails));

        mockMvc.perform(post(BASE_URL + "/{cardId}/request-block", cardId)
                        .with(user(userDetails))
                        .with(csrf()))
                .andExpect(status().isConflict());

        verify(cardService, times(1))
                .requestBlockCard(eq(cardId), eq(userDetails));
    }

    @Test
    void transferMoney_ShouldReturn204_WhenSuccessful() throws Exception {
        doNothing().when(cardService).transferMoney(eq(userDetails), eq(transferBetweenCardsRequest));

        mockMvc.perform(post(BASE_URL + "/transfer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(transferBetweenCardsRequest))
                        .with(user(userDetails))
                        .with(csrf()))
                .andExpect(status().isNoContent());

        verify(cardService, times(1))
                .transferMoney(eq(userDetails), eq(transferBetweenCardsRequest));
    }

    private static Stream<Arguments> provideExceptionsForTransferMoney() {
        return Stream.of(
                Arguments.of(new SelfTransferNotAllowedException(), 400),
                Arguments.of(new CardNotFoundException(""), 404),
                Arguments.of(new InactiveCardException(), 400),
                Arguments.of(new CardOwnershipException(""), 400),
                Arguments.of(new InsufficientFundsException(), 400)
        );
    }

    @ParameterizedTest
    @MethodSource("provideExceptionsForTransferMoney")
    void transferMoney_ShouldReturnCorrectStatus_WhenThrowException(AbstractHttpStatusException ex, int statusCode) throws Exception {
        doThrow(ex)
                .when(cardService)
                .transferMoney(eq(userDetails), any(TransferBetweenCardsRequest.class));

        mockMvc.perform(post(BASE_URL + "/transfer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(transferBetweenCardsRequest))
                        .with(user(userDetails))
                        .with(csrf()))
                .andExpect(status().is(statusCode));

        verify(cardService, times(1))
                .transferMoney(eq(userDetails), any(TransferBetweenCardsRequest.class));
    }
}
