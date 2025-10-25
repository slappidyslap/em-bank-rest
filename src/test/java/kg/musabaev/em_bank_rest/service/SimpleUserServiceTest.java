package kg.musabaev.em_bank_rest.service;

import kg.musabaev.em_bank_rest.dto.*;
import kg.musabaev.em_bank_rest.entity.User;
import kg.musabaev.em_bank_rest.exception.PasswordValidationException;
import kg.musabaev.em_bank_rest.exception.UserAlreadyExistsException;
import kg.musabaev.em_bank_rest.exception.UserNotFoundException;
import kg.musabaev.em_bank_rest.mapper.UserMapper;
import kg.musabaev.em_bank_rest.repository.UserRepository;
import kg.musabaev.em_bank_rest.security.Role;
import kg.musabaev.em_bank_rest.security.SimpleUserDetails;
import kg.musabaev.em_bank_rest.repository.specification.UserSpecification;
import kg.musabaev.em_bank_rest.service.impl.SimpleUserService;
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
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SimpleUserServiceTest {

    @Mock
    private UserRepository userRepo;
    @Mock
    private UserMapper userMapper;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private AuthService authService; // Мок для отзыва токенов

    @InjectMocks
    private SimpleUserService userService;

    private User user;
    private SimpleUserDetails userDetails;
    private GetCreatePatchUserResponse getCreatePatchUserResponse;
    private final Long USER_ID = 1L;
    private final String NEW_EMAIL = "new@email.com";
    private final String OLD_EMAIL = "old@email.com";
    private final String OLD_PASS = "oldPass";
    private final String ENC_OLD_PASS = "encodedOldPass";
    private final String NEW_PASS = "newPass";

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(USER_ID)
                .email(OLD_EMAIL)
                .password(ENC_OLD_PASS)
                .role(Role.USER)
                .build();
        userDetails = new SimpleUserDetails(user);
        getCreatePatchUserResponse = GetCreatePatchUserResponse.builder()
                .id(USER_ID)
                .email(NEW_EMAIL)
                .build();

        lenient().when(userMapper.toGetUserResponse(any(User.class))).thenReturn(getCreatePatchUserResponse);
        lenient().when(userMapper.toPatchUserResponse(any(User.class))).thenReturn(getCreatePatchUserResponse);
        lenient().doNothing().when(userMapper).patch(any(PatchUserRequest.class), any(User.class));

        lenient().when(userRepo.saveAndFlush(any(User.class))).thenReturn(user);
        lenient().when(userRepo.save(any(User.class))).thenReturn(user);
    }

    @Test
    @DisplayName("Get All: Should return page of users")
    @SuppressWarnings("unchecked")
    void getAll_ShouldReturnPageOfUsers() {
        var pageable = Pageable.ofSize(10);
        var filters = mock(UserSpecification.class);
        var userList = List.of(user);
        var userPage = new PageImpl<>(userList, pageable, 1);

        when(filters.build()).thenReturn(null);
        when(userRepo.findAll(isNull(Specification.class), eq(pageable)))
                .thenReturn(userPage);

        var result = userService.getAll(filters, pageable);

        assertThat(result.getContent()).hasSize(1);
        verify(userRepo, times(1))
                .findAll(isNull(Specification.class), eq(pageable));
    }

    @Test
    @DisplayName("Get By Id For Admin: Should return user")
    void getByIdForAdmin_ShouldReturnResponse_WhenUserExists() {
        when(userRepo.findById(USER_ID)).thenReturn(Optional.of(user));

        var result = userService.getByIdForAdmin(USER_ID);

        assertThat(result.id()).isEqualTo(getCreatePatchUserResponse.id());
        verify(userRepo, times(1)).findById(eq(USER_ID));
    }

    @Test
    @DisplayName("Get By Id For Admin: Should throw UserNotFoundException when user does not exist")
    void getByIdForAdmin_ShouldThrowUserNotFoundException_WhenUserDoesNotExist() {
        long nonExistingUserId = 99L;
        when(userRepo.findById(nonExistingUserId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getByIdForAdmin(nonExistingUserId))
                .isInstanceOf(UserNotFoundException.class);

        verify(userRepo, times(1)).findById(nonExistingUserId);
    }

    @Test
    @DisplayName("Patch For Admin: Should update existing user with same email")
    void patchForAdmin_ShouldSucceed_WhenSameEmail() {
        // OLD_EMAIL == user.getEmail()
        var patchUserRequest = PatchUserRequest.builder()
                .email(OLD_EMAIL)
                .fullName("Updated Full Name")
                .build();

        when(userRepo.findById(USER_ID)).thenReturn(Optional.of(user));

        userService.patchForAdmin(USER_ID, patchUserRequest);

        verify(userRepo, times(1)).findById(eq(USER_ID));
        verify(userRepo, never()).existsByEmail(any());
        verify(userRepo, times(1)).saveAndFlush(eq(user));
        verify(authService, times(1)).revokeAllUserRefreshTokens(eq(USER_ID));
    }

    @Test
    @DisplayName("Patch For Admin: Should update existing user with new email that does not exist")
    void patchForAdmin_ShouldSucceed_WhenNewEmailDoesNotExist() {
        // OLD_EMAIL != user.getEmail()
        var patchUserRequest = PatchUserRequest.builder()
                .email(NEW_EMAIL)
                .fullName("Updated Full Name")
                .build();

        when(userRepo.findById(USER_ID)).thenReturn(Optional.of(user));
        when(userRepo.existsByEmail(NEW_EMAIL)).thenReturn(false);

        userService.patchForAdmin(USER_ID, patchUserRequest);

        verify(userRepo, times(1)).findById(eq(USER_ID));
        verify(userRepo, times(1)).existsByEmail(eq(NEW_EMAIL));
        verify(userRepo, times(1)).saveAndFlush(eq(user));
        verify(authService, times(1)).revokeAllUserRefreshTokens(eq(USER_ID));
    }

    @Test
    @DisplayName("Patch For Admin: Should throw UserNotFoundException when patching non existent user")
    void patchForAdmin_ShouldThrowUserNotFoundException() {
        var patchUserRequest = PatchUserRequest.builder().build();

        when(userRepo.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.patchForAdmin(99L, patchUserRequest))
                .isInstanceOf(UserNotFoundException.class);

        verify(userRepo, times(1)).findById(eq(99L));
        verify(userRepo, never()).existsByEmail(any());
        verify(userRepo, never()).saveAndFlush(any());
        verify(authService, never()).revokeAllUserRefreshTokens(any());
    }

    @Test
    @DisplayName("Patch For Admin: Should throw UserAlreadyExistsException when new email already exists")
    void patchForAdmin_ShouldThrowUserAlreadyExistsException() {
        var patchUserRequest = PatchUserRequest.builder()
                .email(NEW_EMAIL)
                .build();

        when(userRepo.findById(USER_ID)).thenReturn(Optional.of(user));
        when(userRepo.existsByEmail(NEW_EMAIL)).thenReturn(true);

        assertThatThrownBy(() -> userService.patchForAdmin(USER_ID, patchUserRequest))
                .isInstanceOf(UserAlreadyExistsException.class);

        verify(userRepo, times(1)).findById(eq(USER_ID));
        verify(userRepo, times(1)).existsByEmail(eq(NEW_EMAIL));
        verify(userRepo, never()).saveAndFlush(any());
        verify(authService, never()).revokeAllUserRefreshTokens(any());
    }

    @Test
    @DisplayName("Get By Id For User: Should return authenticated user")
    void getByIdForUser_ShouldReturnUserForAuthenticatedUser() {
        var result = userService.getByIdForUser(userDetails);

        assertThat(result.id()).isEqualTo(userDetails.getUser().getId());
        verify(userRepo, never()).findById(any());
    }

    @Test
    @DisplayName("Patch For User: Should succeed for authenticated user")
    void patchForUser_ShouldSucceed() {
        var patchUserRequest = PatchUserRequest.builder()
                .email(NEW_EMAIL)
                .fullName("Updated Full Name")
                .build();

        userService.patchForUser(patchUserRequest, userDetails);

        verify(userRepo, never()).findById(any());
        verify(userRepo, times(1)).saveAndFlush(eq(userDetails.getUser()));
        verify(authService, times(1)).revokeAllUserRefreshTokens(eq(USER_ID));
    }

    @Test
    @DisplayName("Update Password: Should update password when old password matches")
    void updatePassword_ShouldSucceed_WhenOldPasswordMatches() {
        var updatePasswordRequest = UpdatePasswordRequest.builder()
                .oldPassword(OLD_PASS)
                .newPassword(NEW_PASS)
                .build();

        when(passwordEncoder.matches(OLD_PASS, user.getPassword())).thenReturn(true);
        when(passwordEncoder.encode(NEW_PASS)).thenReturn("ENCODED_NEW_PASS");

        userService.updatePassword(updatePasswordRequest, userDetails);

        verify(passwordEncoder, times(1)).matches(eq(OLD_PASS), eq(ENC_OLD_PASS));
        verify(passwordEncoder, times(1)).encode(NEW_PASS);
        verify(userRepo, times(1)).save(eq(user));
        verify(authService, times(1)).revokeAllUserRefreshTokens(eq(USER_ID));
    }

    @Test
    @DisplayName("Update Password: Should throw PasswordValidationException when old password does not match")
    void updatePassword_ShouldThrowPasswordValidationException() {
        var updatePasswordRequest = UpdatePasswordRequest.builder()
                .oldPassword(OLD_PASS)
                .newPassword(NEW_PASS)
                .build();

        when(passwordEncoder.matches(OLD_PASS, user.getPassword())).thenReturn(false);

        assertThatThrownBy(() -> userService.updatePassword(updatePasswordRequest, userDetails))
                .isInstanceOf(PasswordValidationException.class);

        verify(passwordEncoder, times(1)).matches(eq(OLD_PASS), eq(user.getPassword()));
        verify(passwordEncoder, never()).encode(any());
        verify(userRepo, never()).save(any());
        verify(authService, never()).revokeAllUserRefreshTokens(any());
    }

    @Test
    @DisplayName("Delete: Should delete user")
    void delete_ShouldDeleteUser() {
        when(userRepo.existsById(USER_ID)).thenReturn(true);

        userService.delete(USER_ID);

        verify(userRepo, times(1)).deleteById(eq(USER_ID));
    }

    @Test
    @DisplayName("Delete: Should throw UserNotFoundException when user not found")
    void delete_ShouldThrowUserNotFoundException_WhenUserNotExists() {
        when(userRepo.existsById(USER_ID)).thenReturn(false);

        assertThatThrownBy(() -> userService.delete(USER_ID))
                .isInstanceOf(UserNotFoundException.class);

        verify(userRepo, never()).deleteById(eq(USER_ID));
    }
}
