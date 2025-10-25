package kg.musabaev.em_bank_rest.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import kg.musabaev.em_bank_rest.dto.CreateCardRequest;
import kg.musabaev.em_bank_rest.dto.GetCreatePatchCardResponse;
import kg.musabaev.em_bank_rest.dto.UpdateStatusCardRequest;
import kg.musabaev.em_bank_rest.entity.CardStatus;
import kg.musabaev.em_bank_rest.entity.User;
import kg.musabaev.em_bank_rest.exception.*;
import kg.musabaev.em_bank_rest.security.JwtAuthFilter;
import kg.musabaev.em_bank_rest.security.Role;
import kg.musabaev.em_bank_rest.security.SimpleUserDetails;
import kg.musabaev.em_bank_rest.service.CardService;
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

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Stream;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        value = AdminCardController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                value = {JwtAuthFilter.class}))
@WithMockUser(roles = {"ADMIN"})
class AdminCardControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CardService cardService;

    private final String BASE_URL = "/api/v1/admin/cards";
    private GetCreatePatchCardResponse mockGetCreatePatchCardResponse;
    private SimpleUserDetails userDetails;

    @BeforeEach
    void setUp() {
        mockGetCreatePatchCardResponse = GetCreatePatchCardResponse.builder()
                .id(101L)
                .numberMasked("************5678")
                .status(CardStatus.ACTIVE)
                .expiry(LocalDate.now().plusYears(3))
                .owner("ELDIYAROV MUSABAI")
                .build();
        userDetails = new SimpleUserDetails(User.builder().id(1L).role(Role.ADMIN).build());
    }

    @Test
    void createCard_ShouldReturn201_WhenSuccessful() throws Exception {
        var request = new CreateCardRequest(5L);

        when(cardService.create(any(CreateCardRequest.class))).thenReturn(mockGetCreatePatchCardResponse);

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf()))
                .andExpect(status().isCreated());

        verify(cardService, times(1)).create(any(CreateCardRequest.class));
    }

    @Test
    void createCard_ShouldReturn404_WhenUserDoesNotExist() throws Exception {
        var request = new CreateCardRequest(99L);

        when(cardService.create(any(CreateCardRequest.class))).thenThrow(new UserNotFoundException(99L));

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf()))
                .andExpect(status().isNotFound());

        verify(cardService, times(1)).create(any(CreateCardRequest.class));
    }

    @Test
    void getAllCards_ShouldReturn200_WithPagedData() throws Exception {
        var pagedModel = new PagedModel<>(new PageImpl<>(List.of(mockGetCreatePatchCardResponse)));

        when(cardService.getAllForAdmin(any(), any(Pageable.class))).thenReturn(pagedModel);

        mockMvc.perform(get(BASE_URL + "?page=0&size=10"))
                .andExpect(status().isOk());

        verify(cardService, times(1)).getAllForAdmin(any(), any(Pageable.class));
    }

    @Test
    void getCardById_ShouldReturn200_WhenCardExists() throws Exception {
        long cardId = 101L;
        when(cardService.getByIdForAdmin(cardId)).thenReturn(mockGetCreatePatchCardResponse);

        mockMvc.perform(get(BASE_URL + "/{cardId}", cardId))
                .andExpect(status().isOk());

        verify(cardService, times(1)).getByIdForAdmin(cardId);
    }

    @Test
    void getCardById_ShouldReturn404_WhenCardDoesNotExist() throws Exception {
        long cardId = 999L;
        when(cardService.getByIdForAdmin(cardId)).thenThrow(new CardNotFoundException(cardId));

        mockMvc.perform(get(BASE_URL + "/{cardId}", cardId))
                .andExpect(status().isNotFound());

        verify(cardService, times(1)).getByIdForAdmin(cardId);
    }

    @Test
    void deleteCard_ShouldReturn204_WhenSuccessful() throws Exception {
        long cardId = 101L;
        doNothing().when(cardService).delete(cardId);

        mockMvc.perform(delete(BASE_URL + "/{cardId}", cardId)
                        .with(csrf()))
                .andExpect(status().isNoContent());

        verify(cardService, times(1)).delete(cardId);
    }

    @Test
    void deleteCard_ShouldReturn404_WhenCardDoesNotExist() throws Exception {
        long cardId = 999L;
        doThrow(new CardNotFoundException(cardId)).when(cardService).delete(cardId);

        mockMvc.perform(delete(BASE_URL + "/{cardId}", cardId)
                        .with(csrf()))
                .andExpect(status().isNotFound());

        verify(cardService, times(1)).delete(cardId);
    }

    @Test
    void updateCardStatus_ShouldReturn200_WhenSuccessful() throws Exception {
        long cardId = 101L;
        var request = new UpdateStatusCardRequest(CardStatus.BLOCKED);

        when(cardService.patchStatusForAdmin(eq(cardId), any(UpdateStatusCardRequest.class), any(SimpleUserDetails.class)))
                .thenReturn(mockGetCreatePatchCardResponse);

        mockMvc.perform(patch(BASE_URL + "/{cardId}", cardId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(user(userDetails))
                        .with(csrf()))
                .andExpect(status().isOk());

        verify(cardService, times(1)).patchStatusForAdmin(eq(cardId), any(), any());
    }

    private static Stream<Arguments> provideExceptionsForPatch() {
        return Stream.of(
                Arguments.of(new CardBlockRequestNotFoundException(99L), 404),
                Arguments.of(new CardUnsupportedOperationException(""), 409),
                Arguments.of(new CardExpiredException(), 400)
        );
    }

    @ParameterizedTest
    @MethodSource("provideExceptionsForPatch")
    void updateCardStatus_ShouldReturn400_WhenThrowException(AbstractHttpStatusException ex, int statusCode) throws Exception {
        long cardId = 101L;
        var request = new UpdateStatusCardRequest(CardStatus.BLOCKED);

        when(cardService.patchStatusForAdmin(eq(cardId), any(UpdateStatusCardRequest.class), any(SimpleUserDetails.class)))
                .thenThrow(ex);

        mockMvc.perform(patch(BASE_URL + "/{cardId}", cardId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(user(userDetails))
                        .with(csrf()))
                .andExpect(status().is(statusCode));

        verify(cardService, times(1)).patchStatusForAdmin(eq(cardId), any(), any());
    }
}
