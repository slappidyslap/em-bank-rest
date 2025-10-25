package kg.musabaev.em_bank_rest.service.impl;

import kg.musabaev.em_bank_rest.dto.AccessAndRefreshTokensResponse;
import kg.musabaev.em_bank_rest.dto.AuthenticateRequest;
import kg.musabaev.em_bank_rest.dto.GetCreatePatchUserResponse;
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
import kg.musabaev.em_bank_rest.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class SimpleAuthService implements AuthService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    private final RefreshTokenRepository refreshTokenRepository;

    @Override
    @Transactional
    public GetCreatePatchUserResponse signup(SignupUserRequest dto) {
        if (userRepository.existsByEmail(dto.email()))
            throw new UserAlreadyExistsException();

        var user = User.builder()
                .fullName(dto.fullName())
                .email(dto.email())
                .password(passwordEncoder.encode(dto.password()))
                .role(Role.USER)
                .build();
        var persistedUser = userRepository.save(user);

        return userMapper.toCreateUserResponse(persistedUser);
    }

    @Override
    @Transactional
    public AccessAndRefreshTokensResponse login(AuthenticateRequest dto) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                dto.email(),
                dto.password()
        ));
        return new AccessAndRefreshTokensResponse(
                jwtUtil.generateAccessToken(dto.email()),
                jwtUtil.generateRefreshToken(dto.email()));
    }

    @Override
    @Transactional
    public AccessAndRefreshTokensResponse refresh(String refreshToken) {
        RefreshToken refreshTokenEntity = refreshTokenRepository
                .findByToken(refreshToken)
                .orElseThrow(RefreshTokenNotFoundException::new);

        if (Instant.now().isAfter(refreshTokenEntity.getExpiration()))
            throw new RefreshTokenExpiredException();

        String email = refreshTokenRepository.findRefreshTokenOwnerEmailByToken(refreshToken)
                .orElseThrow(() -> new UserNotFoundException(refreshToken, "refresh token"));
        refreshTokenRepository.deleteById(refreshTokenEntity.getId());
        return new AccessAndRefreshTokensResponse(
                jwtUtil.generateAccessToken(email),
                jwtUtil.generateRefreshToken(email)
        );
    }

    @Override
    @Transactional
    public void revokeAllUserRefreshTokens(Long userId) {
        refreshTokenRepository.deleteByOwner_Id(userId);
    }
}
