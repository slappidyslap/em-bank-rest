package kg.musabaev.em_bank_rest.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import kg.musabaev.em_bank_rest.dto.GetCreatePatchUserResponse;
import kg.musabaev.em_bank_rest.dto.PatchUserRequest;
import kg.musabaev.em_bank_rest.exception.AbstractHttpStatusException;
import kg.musabaev.em_bank_rest.exception.UserAlreadyExistsException;
import kg.musabaev.em_bank_rest.exception.UserNotFoundException;
import kg.musabaev.em_bank_rest.security.JwtAuthFilter;
import kg.musabaev.em_bank_rest.security.Role;
import kg.musabaev.em_bank_rest.service.UserService;
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

import java.util.List;
import java.util.stream.Stream;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        value = AdminUserController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                value = {JwtAuthFilter.class}))
@WithMockUser(roles = {"ADMIN"})
class AdminUserControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserService userService;

    private final String BASE_URL = "/api/v1/admin/users";
    private GetCreatePatchUserResponse mockGetCreatePatchUserResponse;
    private PatchUserRequest patchUserRequest;

    @BeforeEach
    void setUp() {
        mockGetCreatePatchUserResponse = GetCreatePatchUserResponse.builder()
                .id(1L)
                .email("test@admin.com")
                .fullName("MUSABAEV ELDIYAR")
                .role(Role.ADMIN)
                .build();
        patchUserRequest = PatchUserRequest.builder()
                .email("new@email.com")
                .fullName("NewName").build();
    }

    @Test
    void getAllUser_ShouldReturn200_WithPagedData() throws Exception {
        var pagedModel = new PagedModel<>(new PageImpl<>(List.of(mockGetCreatePatchUserResponse)));

        when(userService.getAll(any(), any(Pageable.class))).thenReturn(pagedModel);

        mockMvc.perform(get(BASE_URL + "?page=0&size=10"))
                .andExpect(status().isOk());

        verify(userService, times(1)).getAll(any(), any(Pageable.class));
    }

    @Test
    void getUserById_ShouldReturn200_WhenUserExists() throws Exception {
        long userId = 1L;
        when(userService.getByIdForAdmin(userId)).thenReturn(mockGetCreatePatchUserResponse);

        mockMvc.perform(get(BASE_URL + "/{userId}", userId))
                .andExpect(status().isOk());

        verify(userService, times(1)).getByIdForAdmin(userId);
    }

    @Test
    void getUserById_ShouldReturn404_WhenUserDoesNotExist() throws Exception {
        long userId = 99L;
        when(userService.getByIdForAdmin(userId)).thenThrow(new UserNotFoundException(userId));

        mockMvc.perform(get(BASE_URL + "/{userId}", userId))
                .andExpect(status().isNotFound());

        verify(userService, times(1)).getByIdForAdmin(userId);
    }

    @Test
    void deleteUser_ShouldReturn204_WhenSuccessful() throws Exception {
        long userId = 1L;
        doNothing().when(userService).delete(userId);

        mockMvc.perform(delete(BASE_URL + "/{userId}", userId)
                        .with(csrf()))
                .andExpect(status().isNoContent());

        verify(userService, times(1)).delete(userId);
    }

    @Test
    void deleteUser_ShouldReturn404_WhenUserDoesNotExist() throws Exception {
        long userId = 99L;
        doThrow(new UserNotFoundException(userId)).when(userService).delete(userId);

        mockMvc.perform(delete(BASE_URL + "/{userId}", userId)
                        .with(csrf()))
                .andExpect(status().isNotFound());

        verify(userService, times(1)).delete(userId);
    }

    @Test
    void updateUser_ShouldReturn200_WhenSuccessful() throws Exception {
        long userId = 1L;

        when(userService.patchForAdmin(eq(userId), any(PatchUserRequest.class)))
                .thenReturn(mockGetCreatePatchUserResponse);

        mockMvc.perform(patch(BASE_URL + "/{userId}", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(patchUserRequest))
                        .with(csrf()))
                .andExpect(status().isOk());

        verify(userService, times(1)).patchForAdmin(eq(userId), any(PatchUserRequest.class));
    }

    private static Stream<Arguments> provideExceptionsForPatch() {
        return Stream.of(
                Arguments.of(new UserNotFoundException(99L), 404),
                Arguments.of(new UserAlreadyExistsException(), 409)
        );
    }

    @ParameterizedTest
    @MethodSource("provideExceptionsForPatch")
    void updateUser_ShouldReturn404_WhenThrowException(AbstractHttpStatusException ex, int statusCode) throws Exception {
        long userId = 99L;
        when(userService.patchForAdmin(eq(userId), any(PatchUserRequest.class)))
                .thenThrow(ex);

        mockMvc.perform(patch(BASE_URL + "/{userId}", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(patchUserRequest))
                        .with(csrf()))
                .andExpect(status().is(statusCode));

        verify(userService, times(1)).patchForAdmin(eq(userId), any(PatchUserRequest.class));
    }
}
