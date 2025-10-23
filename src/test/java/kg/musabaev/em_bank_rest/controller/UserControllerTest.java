package kg.musabaev.em_bank_rest.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import kg.musabaev.em_bank_rest.dto.GetCreatePatchUserResponse;
import kg.musabaev.em_bank_rest.dto.PatchUserRequest;
import kg.musabaev.em_bank_rest.dto.UpdatePasswordRequest;
import kg.musabaev.em_bank_rest.entity.User;
import kg.musabaev.em_bank_rest.exception.PasswordValidationException;
import kg.musabaev.em_bank_rest.exception.UserAlreadyExistsException;
import kg.musabaev.em_bank_rest.security.JwtAuthFilter;
import kg.musabaev.em_bank_rest.security.Role;
import kg.musabaev.em_bank_rest.security.SimpleUserDetails;
import kg.musabaev.em_bank_rest.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        value = UserController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                value = {JwtAuthFilter.class}))
@WithMockUser(roles = {"USER"})
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserService userService;

    private final String BASE_URL = "/api/v1/users/me";
    private GetCreatePatchUserResponse mockUserResponse;
    private SimpleUserDetails userDetails;

    @BeforeEach
    void setUp() {
        mockUserResponse = GetCreatePatchUserResponse.builder()
                .id(1L)
                .email("test@me.com")
                .fullName("Test User")
                .role(Role.USER)
                .build();

        userDetails = new SimpleUserDetails(
                User.builder()
                        .id(1L)
                        .role(Role.USER)
                        .password("encoded_password") // Для проверки пароля
                        .build());
    }

    @Test
    void getMe_ShouldReturn200_WhenSuccessful() throws Exception {
        when(userService.getByIdForUser(eq(userDetails))).thenReturn(mockUserResponse);

        mockMvc.perform(get(BASE_URL)
                        .with(user(userDetails)))
                .andExpect(status().isOk());

        verify(userService, times(1)).getByIdForUser(eq(userDetails));
    }

    @Test
    void updateUser_ShouldReturn200_WhenSuccessful() throws Exception {
        var request = PatchUserRequest.builder()
                .email("new@email.com")
                .fullName("New Name")
                .build();

        when(userService.patchForUser(any(PatchUserRequest.class), eq(userDetails)))
                .thenReturn(mockUserResponse);

        mockMvc.perform(patch(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(user(userDetails))
                        .with(csrf()))
                .andExpect(status().isOk());

        verify(userService, times(1)).patchForUser(any(PatchUserRequest.class), eq(userDetails));
    }

    @Test
    void updateUser_ShouldReturnCorrectStatus_WhenThrowException() throws Exception {
        var request = PatchUserRequest.builder()
                .email("new@email.com")
                .fullName("New Name")
                .build();

        when(userService.patchForUser(any(PatchUserRequest.class), eq(userDetails)))
                .thenThrow(new UserAlreadyExistsException());

        mockMvc.perform(patch(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(user(userDetails))
                        .with(csrf()))
                .andExpect(status().isConflict());

        verify(userService, times(1)).patchForUser(any(PatchUserRequest.class), eq(userDetails));
    }

    @Test
    void updatePassword_ShouldReturn204_WhenSuccessful() throws Exception {
        var request = new UpdatePasswordRequest("old_pass", "new_pass");
        doNothing().when(userService).updatePassword(any(UpdatePasswordRequest.class), eq(userDetails));

        mockMvc.perform(patch(BASE_URL + "/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(user(userDetails))
                        .with(csrf()))
                .andExpect(status().isNoContent());

        verify(userService, times(1))
                .updatePassword(any(UpdatePasswordRequest.class), eq(userDetails));
    }

    @Test
    void updatePassword_ShouldReturnCorrectStatus_WhenErrorOccurs() throws Exception {
        var request = new UpdatePasswordRequest("wrong_pass", "new_pass");

        doThrow(new PasswordValidationException(""))
                .when(userService)
                .updatePassword(any(UpdatePasswordRequest.class), eq(userDetails));

        mockMvc.perform(patch(BASE_URL + "/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(user(userDetails))
                        .with(csrf()))
                .andExpect(status().isBadRequest());

        verify(userService, times(1)).updatePassword(any(UpdatePasswordRequest.class), eq(userDetails));
    }
}
