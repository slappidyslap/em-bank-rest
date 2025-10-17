package kg.musabaev.em_bank_rest.service.impl;

import kg.musabaev.em_bank_rest.dto.GetCreatePatchUserResponse;
import kg.musabaev.em_bank_rest.dto.PatchUserRequest;
import kg.musabaev.em_bank_rest.entity.User;
import kg.musabaev.em_bank_rest.exception.UserAlreadyExistsException;
import kg.musabaev.em_bank_rest.exception.UserNotFoundException;
import kg.musabaev.em_bank_rest.mapper.UserMapper;
import kg.musabaev.em_bank_rest.repository.UserRepository;
import kg.musabaev.em_bank_rest.repository.specification.UserSpecification;
import kg.musabaev.em_bank_rest.security.SimpleUserDetails;
import kg.musabaev.em_bank_rest.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedModel;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SimpleUserService implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Override
    @Transactional(readOnly = true)
    public PagedModel<GetCreatePatchUserResponse> getAll(UserSpecification filters, Pageable pageable) {
        var users = userRepository.findAll(filters.build(), pageable);
        return new PagedModel<>(users.map(userMapper::toGetUserResponse));
    }

    @Override
    @Transactional
    public GetCreatePatchUserResponse patch(Long id, PatchUserRequest dto) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
        if (dto.email() != null && userRepository.existsByEmail(dto.email()))
            throw new UserAlreadyExistsException();

        userMapper.patch(dto, user);
        var persistedUser = userRepository.save(user);
        return userMapper.toPatchUserResponse(persistedUser);
    }

    @Override
    @Transactional(readOnly = true)
    public GetCreatePatchUserResponse getById(Authentication auth) {
        var authUser = getUserByAuthentication(auth);
        return userMapper.toGetUserResponse(authUser);
    }

    @Override
    @Transactional
    public GetCreatePatchUserResponse patch(PatchUserRequest dto, Authentication auth) {
        var authUser = getUserByAuthentication(auth);
        userMapper.patch(dto, authUser);
        var persistedUser = userRepository.save(authUser);
        return userMapper.toPatchUserResponse(persistedUser);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
        userRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public GetCreatePatchUserResponse getById(Long id) {
        return userMapper.toGetUserResponse(userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id)));
    }

    private User getUserByAuthentication(Authentication auth) {
        var userDetails = (SimpleUserDetails) auth.getPrincipal();
        return userDetails.getUser();
    }
}
