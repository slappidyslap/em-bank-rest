package kg.musabaev.em_bank_rest.service.impl;

import kg.musabaev.em_bank_rest.dto.PatchUserRequest;
import kg.musabaev.em_bank_rest.entity.User;
import kg.musabaev.em_bank_rest.exception.UserAlreadyExistsException;
import kg.musabaev.em_bank_rest.exception.UserNotFoundException;
import kg.musabaev.em_bank_rest.mapper.UserMapper;
import kg.musabaev.em_bank_rest.repository.UserRepository;
import kg.musabaev.em_bank_rest.repository.specification.UserSpecification;
import kg.musabaev.em_bank_rest.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SimpleUserService implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Override
    @Transactional(readOnly = true)
    public ResponseEntity<Page<User>> getAll(UserSpecification spec, Pageable pageable) {
        return ResponseEntity.ok(userRepository.findAll(spec.build(), pageable));
    }

    @Override
    public ResponseEntity<User> patch(Long id, PatchUserRequest dto) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
        if (dto.email() != null && userRepository.existsByEmail(dto.email()))
            throw new UserAlreadyExistsException();

        userMapper.patch(dto, user);
        return ResponseEntity.ok(userRepository.save(user));
    }

    @Override
    @Transactional
    public ResponseEntity<?> delete(Long id) {
        userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
        userRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseEntity<User> getById(Long id) {
        return ResponseEntity.ok(userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id)));
    }
}
