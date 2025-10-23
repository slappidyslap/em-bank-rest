package kg.musabaev.em_bank_rest.service.impl;

import kg.musabaev.em_bank_rest.dto.GetCreatePatchUserResponse;
import kg.musabaev.em_bank_rest.dto.PatchUserRequest;
import kg.musabaev.em_bank_rest.dto.UpdatePasswordRequest;
import kg.musabaev.em_bank_rest.entity.User;
import kg.musabaev.em_bank_rest.exception.PasswordValidationException;
import kg.musabaev.em_bank_rest.exception.UserAlreadyExistsException;
import kg.musabaev.em_bank_rest.exception.UserNotFoundException;
import kg.musabaev.em_bank_rest.mapper.UserMapper;
import kg.musabaev.em_bank_rest.repository.UserRepository;
import kg.musabaev.em_bank_rest.repository.specification.UserSpecification;
import kg.musabaev.em_bank_rest.security.SimpleUserDetails;
import kg.musabaev.em_bank_rest.service.AuthService;
import kg.musabaev.em_bank_rest.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedModel;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SimpleUserService implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final AuthService authService;

    @Override
    @Transactional(readOnly = true)
    public PagedModel<GetCreatePatchUserResponse> getAll(UserSpecification filters, Pageable pageable) {
        var users = userRepository.findAll(filters.build(), pageable);
        return new PagedModel<>(users.map(userMapper::toGetUserResponse));
    }

    @Override
    @Transactional
    public GetCreatePatchUserResponse patchForAdmin(Long id, PatchUserRequest dto) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));

        if (!user.getEmail().equalsIgnoreCase(dto.email())
                && userRepository.existsByEmail(dto.email()))
            throw new UserAlreadyExistsException();

        return patchUser(dto, user);
    }

    private GetCreatePatchUserResponse patchUser(PatchUserRequest dto, User user) {
        userMapper.patch(dto, user);
        var persistedUser = userRepository.saveAndFlush(user);
        authService.revokeAllUserRefreshTokens(user.getId());
        return userMapper.toPatchUserResponse(persistedUser);
    }

    @Override
    @Transactional(readOnly = true)
    public GetCreatePatchUserResponse getByIdForUser(SimpleUserDetails userDetails) {
        var authUser = userDetails.getUser();
        return userMapper.toGetUserResponse(authUser);
    }

    @Override
    @Transactional
    public GetCreatePatchUserResponse patchForUser(PatchUserRequest dto, SimpleUserDetails userDetails) {
        var authUser = userDetails.getUser();
        return patchUser(dto, authUser);
    }

    @Override
    @Transactional
    public void updatePassword(UpdatePasswordRequest dto, SimpleUserDetails userDetails) {
        var authUser = userDetails.getUser();
        var oldPassword = dto.oldPassword();
        var newPassword = dto.newPassword();

        if (!passwordEncoder.matches(oldPassword, authUser.getPassword()))
            throw new PasswordValidationException("Invalid old password");

        authUser.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(authUser);

        authService.revokeAllUserRefreshTokens(authUser.getId());
    }

    @Override
    @Transactional
    public void delete(Long id) {
        if (userRepository.existsById(id))
                throw new UserNotFoundException(id);
        userRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public GetCreatePatchUserResponse getByIdForAdmin(Long id) {
        return userMapper.toGetUserResponse(userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id)));
    }
}
