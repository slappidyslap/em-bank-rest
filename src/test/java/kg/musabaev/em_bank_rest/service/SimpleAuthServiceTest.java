package kg.musabaev.em_bank_rest.service;

import kg.musabaev.em_bank_rest.dto.AuthenticateRequest;
import kg.musabaev.em_bank_rest.dto.SignupUserRequest;
import kg.musabaev.em_bank_rest.entity.User;
import kg.musabaev.em_bank_rest.exception.RefreshTokenExpiredException;
import kg.musabaev.em_bank_rest.exception.RefreshTokenNotFoundException;
import kg.musabaev.em_bank_rest.exception.UserAlreadyExistsException;
import kg.musabaev.em_bank_rest.exception.UserNotFoundException;
import kg.musabaev.em_bank_rest.mapper.UserMapper;
import kg.musabaev.em_bank_rest.repository.RefreshTokenRepository;
import kg.musabaev.em_bank_rest.repository.UserRepository;
import kg.musabaev.em_bank_rest.security.JwtUtil;
import kg.musabaev.em_bank_rest.security.RefreshToken;
import kg.musabaev.em_bank_rest.security.Role;
import kg.musabaev.em_bank_rest.service.impl.SimpleAuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SimpleAuthServiceTest {

    @Mock
    private UserRepository userRepo;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private UserMapper userMapper;
    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private JwtUtil jwtUtil;
    @Mock
    private RefreshTokenRepository refreshTokenRepo;

    @InjectMocks
    private SimpleAuthService authService;

    private User user;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .email("user@email.com")
                .fullName("Musbaev Eldiyar")
                .password("123")
                .role(Role.USER)
                .build();
    }

    @Test
    @DisplayName("Signup: Should create user")
    void signup_ShouldCreateUserAndReturnResponse_WhenUserDoesNotExist() {
        var email = "registered@email.com";
        var password = "pass";
        var mockSignupRequest = SignupUserRequest.builder()
                .email(email)
                .fullName("Registered user full name")
                .password(password)
                .build();

        when(userRepo.existsByEmail(email)).thenReturn(false);
        when(passwordEncoder.encode(password)).thenReturn("encoded_pass");
        when(userRepo.save(any(User.class))).thenReturn(user);

        authService.signup(mockSignupRequest);

        verify(userRepo, times(1)).existsByEmail(eq(email));
        verify(passwordEncoder, times(1)).encode(eq(password));
        verify(userRepo, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("Signup: Should throw UserAlreadyExistsException when user already exists")
    void signup_ShouldThrowUserAlreadyExistsException_WhenUserExists() {
        var email = "existing@email.com";
        var dto = SignupUserRequest.builder().email(email).build();

        when(userRepo.existsByEmail(email)).thenReturn(true);

        assertThatThrownBy(() -> authService.signup(dto))
                .isInstanceOf(UserAlreadyExistsException.class);
        verify(userRepo, times(1)).existsByEmail(eq(email));
        verify(passwordEncoder, never()).encode(any());
        verify(userRepo, never()).save(any());
    }

    @Test
    @DisplayName("Login: Should authenticate and return tokens on successful login")
    void login_ShouldAuthenticateAndReturnTokens() {
        var email = "some@email.com";
        var dto = AuthenticateRequest.builder()
                .email(email)
                .password("pass")
                .build();

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(mock(UsernamePasswordAuthenticationToken.class));
        when(jwtUtil.generateAccessToken(email)).thenReturn("accessTokenJwt");
        when(jwtUtil.generateRefreshToken(email)).thenReturn("refreshTokenUuid");

        authService.login(dto);

        verify(authenticationManager, times(1)).authenticate(
                any(UsernamePasswordAuthenticationToken.class));
        verify(jwtUtil, times(1)).generateAccessToken(eq(email));
        verify(jwtUtil, times(1)).generateRefreshToken(eq(email));
    }

    @Test
    @DisplayName("Login: Should throw exception if authentication fails")
    void login_ShouldThrowException_OnFailure() {
        String email = "some@email.com";
        var dto = AuthenticateRequest.builder()
                .email(email)
                .password("pass")
                .build();

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException(""));

        assertThatThrownBy(() -> authService.login(dto))
                .isInstanceOf(BadCredentialsException.class);
        verify(jwtUtil, never()).generateAccessToken(any());
        verify(jwtUtil, never()).generateRefreshToken(any());
    }

    @Test
    @DisplayName("Refresh: Should return new tokens and delete old token when valid")
    void refresh_ShouldReturnNewTokensAndDeleteOldToken_WhenValid() {
        var email = "some@email.com";
        var accessTokenValue = "accessTokenJwt";
        var refreshTokenValue = "refreshTokenUuid";
        var refreshToken = RefreshToken.builder()
                .id(2L)
                .token(refreshTokenValue)
                .expiration(Instant.now().plusSeconds(600))
                .owner(user)
                .build();

        when(refreshTokenRepo.findByToken(refreshTokenValue))
                .thenReturn(Optional.of(refreshToken));
        when(refreshTokenRepo.findRefreshTokenOwnerEmailByToken(refreshTokenValue))
                .thenReturn(Optional.of(email));
        when(jwtUtil.generateAccessToken(email)).thenReturn(accessTokenValue);
        when(jwtUtil.generateRefreshToken(email)).thenReturn(refreshTokenValue);

        authService.refresh(refreshTokenValue);

        verify(refreshTokenRepo, times(1)).findByToken(eq(refreshTokenValue));
        verify(refreshTokenRepo, times(1)).findRefreshTokenOwnerEmailByToken(eq(refreshTokenValue));
        verify(refreshTokenRepo, times(1)).deleteById(eq(refreshToken.getId()));
        verify(jwtUtil, times(1)).generateAccessToken(eq(email));
        verify(jwtUtil, times(1)).generateRefreshToken(eq(email));
    }

    @Test
    @DisplayName("Refresh: Should throw RefreshTokenNotFoundException when token not found")
    void refresh_ShouldThrowRefreshTokenNotFoundException_WhenTokenNotFound() {
        var refreshTokenValue = "someRefreshToken";

        when(refreshTokenRepo.findByToken(refreshTokenValue))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.refresh(refreshTokenValue))
                .isInstanceOf(RefreshTokenNotFoundException.class);
        verify(refreshTokenRepo, times(1)).findByToken(eq(refreshTokenValue));
        verify(refreshTokenRepo, never()).findRefreshTokenOwnerEmailByToken(any());
        verify(refreshTokenRepo, never()).deleteById(any());
    }

    @Test
    @DisplayName("Refresh: Should throw RefreshTokenExpiredException when token expired")
    void refresh_ShouldThrowRefreshTokenExpiredException_WhenTokenExpired() {
        var refreshTokenValue = "someRefreshToken";
        var expiredToken = RefreshToken.builder()
                .token(refreshTokenValue)
                .expiration(Instant.now().minusSeconds(600))
                .owner(user)
                .build();

        when(refreshTokenRepo.findByToken(refreshTokenValue)).thenReturn(Optional.of(expiredToken));

        assertThatThrownBy(() -> authService.refresh(refreshTokenValue))
                .isInstanceOf(RefreshTokenExpiredException.class);

        verify(refreshTokenRepo, times(1)).findByToken(eq(refreshTokenValue));
        verify(refreshTokenRepo, never()).findRefreshTokenOwnerEmailByToken(any());
        verify(refreshTokenRepo, never()).deleteById(any());
    }

    @Test
    @DisplayName("Refresh: Should throw UserNotFoundException when owner email not found")
    void refresh_ShouldThrowUserNotFoundException_WhenOwnerNotFound() {
        var refreshTokenValue = "anotherRefreshToken";
        var refreshToken = RefreshToken.builder()
                .token(refreshTokenValue)
                .expiration(Instant.now().plusSeconds(600))
                .owner(user)
                .build();

        when(refreshTokenRepo.findByToken(refreshTokenValue))
                .thenReturn(Optional.of(refreshToken));
        when(refreshTokenRepo.findRefreshTokenOwnerEmailByToken(refreshTokenValue))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.refresh(refreshTokenValue))
                .isInstanceOf(UserNotFoundException.class);

        verify(refreshTokenRepo, times(1)).findByToken(refreshTokenValue);
        verify(refreshTokenRepo, times(1)).findRefreshTokenOwnerEmailByToken(refreshTokenValue);
        verify(refreshTokenRepo, never()).deleteById(any());
    }

    @Test
    @DisplayName("Revoke: Should delete user's all refresh tokens ")
    void revokeAllUserRefreshTokens_ShouldCallRepositoryDeleteMethod() {
        var userId = 2L;

        doNothing().when(refreshTokenRepo).deleteByOwner_Id(userId);

        authService.revokeAllUserRefreshTokens(userId);

        verify(refreshTokenRepo, times(1)).deleteByOwner_Id(eq(userId));
    }
}
