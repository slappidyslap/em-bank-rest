package kg.musabaev.em_bank_rest.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import kg.musabaev.em_bank_rest.dto.AccessAndRefreshTokensResponse;
import kg.musabaev.em_bank_rest.dto.AuthenticateRequest;
import kg.musabaev.em_bank_rest.dto.GetCreatePatchUserResponse;
import kg.musabaev.em_bank_rest.dto.SignupUserRequest;
import kg.musabaev.em_bank_rest.exception.RefreshTokenExpiredException;
import kg.musabaev.em_bank_rest.exception.RefreshTokenNotFoundException;
import kg.musabaev.em_bank_rest.exception.UserAlreadyExistsException;
import kg.musabaev.em_bank_rest.exception.UserNotFoundException;
import kg.musabaev.em_bank_rest.security.JwtAuthFilter;
import kg.musabaev.em_bank_rest.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.stream.Stream;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.http.HttpHeaders.SET_COOKIE;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
        value = AuthController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                value = {JwtAuthFilter.class}))
@WithMockUser
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AuthService authService;

    private final String BASE_URL = "/api/v1/auth";
    private final String refreshTokenValue = "refreshTokenUuid";
    private AccessAndRefreshTokensResponse mockTokens;
    private GetCreatePatchUserResponse mockGetCreatePatchUserResponse;

    @BeforeEach
    void setUp() {
        mockTokens = AccessAndRefreshTokensResponse.builder()
                .accessToken("accessTokenJwt")
                .refreshToken(refreshTokenValue)
                .build();
        mockGetCreatePatchUserResponse = GetCreatePatchUserResponse.builder()
                .id(1L)
                .email("regitered@user.com")
                .fullName("Registered User")
                .build();
    }

    @Test
    void signup_ShouldReturn200_WhenSuccessful() throws Exception {
        var request = SignupUserRequest.builder()
                .fullName("Registered User")
                .email("registered@user.com")
                .password("password")
                .build();

        when(authService.signup(any(SignupUserRequest.class))).thenReturn(mockGetCreatePatchUserResponse);

        mockMvc.perform(post(BASE_URL + "/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf()))
                .andExpect(status().isCreated());

        verify(authService, times(1)).signup(any(SignupUserRequest.class));
    }

    @Test
    void signup_ShouldReturn409_WhenErrorOccurs() throws Exception {
        var request = SignupUserRequest.builder()
                .fullName("User")
                .email("existing@email.com")
                .password("password")
                .build();

        when(authService.signup(any(SignupUserRequest.class))).thenThrow(new UserAlreadyExistsException());

        mockMvc.perform(post(BASE_URL + "/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf()))
                .andExpect(status().isConflict());

        verify(authService, times(1)).signup(any(SignupUserRequest.class));
    }

    @Test
    void login_ShouldReturn200AndSetCookie_WhenSuccessful() throws Exception {
        var request = AuthenticateRequest.builder()
                .email("user@test.com")
                .password("password")
                .build();

        when(authService.login(any(AuthenticateRequest.class))).thenReturn(mockTokens);

        mockMvc.perform(post(BASE_URL + "/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(header().string(SET_COOKIE, containsString("REFRESH_TOKEN=" + refreshTokenValue)))
                .andExpect(header().string(SET_COOKIE, containsString("HttpOnly")))
                .andExpect(jsonPath("$.accessToken").value(mockTokens.accessToken()));

        verify(authService, times(1)).login(any(AuthenticateRequest.class));
    }

    @Test
    void refresh_ShouldReturn200AndSetCookie_WhenSuccessful() throws Exception {
        when(authService.refresh(eq(refreshTokenValue))).thenReturn(mockTokens);

        mockMvc.perform(post(BASE_URL + "/refresh")
                        .cookie(new Cookie("REFRESH_TOKEN", refreshTokenValue))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(header().string(SET_COOKIE, containsString("REFRESH_TOKEN=" + refreshTokenValue)));

        verify(authService, times(1)).refresh(eq(refreshTokenValue));
    }

    private static Stream<Arguments> provideRefreshExceptions() {
        return Stream.of(
                Arguments.of(new RefreshTokenNotFoundException(), 404),
                Arguments.of(new RefreshTokenExpiredException(), 401),
                Arguments.of(new UserNotFoundException(0L), 404)
        );
    }

    @ParameterizedTest
    @MethodSource("provideRefreshExceptions")
    void refresh_ShouldReturnCorrectStatus_WhenErrorOccurs(RuntimeException ex, int statusCode) throws Exception {

        when(authService.refresh(eq(refreshTokenValue))).thenThrow(ex);

        mockMvc.perform(post(BASE_URL + "/refresh")
                        .cookie(new Cookie("REFRESH_TOKEN", refreshTokenValue))
                        .with(csrf()))
                .andExpect(status().is(statusCode));

        verify(authService, times(1)).refresh(eq(refreshTokenValue));
    }

    @Test
    void refresh_ShouldReturn400_WhenNoCookiePresent() throws Exception {
        mockMvc.perform(post(BASE_URL + "/refresh")
                        .with(csrf()))
                .andExpect(status().isBadRequest());

        verify(authService, times(0)).refresh(any());
    }
}
