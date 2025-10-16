package kg.musabaev.em_bank_rest.service.impl;

import kg.musabaev.em_bank_rest.dto.AuthenticateRefreshUserResponse;
import kg.musabaev.em_bank_rest.dto.AuthenticateRequest;
import kg.musabaev.em_bank_rest.dto.SignupUserRequest;
import kg.musabaev.em_bank_rest.dto.SignupUserResponse;
import kg.musabaev.em_bank_rest.entity.User;
import kg.musabaev.em_bank_rest.exception.UserAlreadyExistsException;
import kg.musabaev.em_bank_rest.exception.UserUnauthorizedException;
import kg.musabaev.em_bank_rest.mapper.UserMapper;
import kg.musabaev.em_bank_rest.repository.UserRepository;
import kg.musabaev.em_bank_rest.security.JwtUtil;
import kg.musabaev.em_bank_rest.security.Role;
import kg.musabaev.em_bank_rest.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SimpleAuthService implements AuthService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;

    @Override
    public SignupUserResponse signup(SignupUserRequest dto) {
        Optional<User> existingUser = userRepository.findByEmail(dto.email());
        if (existingUser.isPresent())
            throw new UserAlreadyExistsException();

        var user = User.builder()
                .fullName(dto.fullName())
                .email(dto.email())
                .password(passwordEncoder.encode(dto.password()))
                .role(Role.USER)
                .build();
        var persistedUser = userRepository.save(user);

        return userMapper.toSignupUserResponse(persistedUser);
    }

    @Override
    public AuthenticateRefreshUserResponse login(AuthenticateRequest dto) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                dto.email(),
                dto.password()
        ));
        return new AuthenticateRefreshUserResponse(jwtUtil.generateToken(dto.email()));
    }

    @Override
    public AuthenticateRefreshUserResponse refresh() {
        return null;
    }
}
