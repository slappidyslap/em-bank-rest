package kg.musabaev.em_bank_rest.security;

import io.jsonwebtoken.Jwts;
import kg.musabaev.em_bank_rest.entity.User;
import kg.musabaev.em_bank_rest.exception.UserNotFoundException;
import kg.musabaev.em_bank_rest.repository.RefreshTokenRepository;
import kg.musabaev.em_bank_rest.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class JwtUtil {

    private final long ACCESS_TOKEN_EXPIRATION = 1000 * 60 * 60; // 1 час
    private final long REFRESH_TOKEN_EXPIRATION = 1000 * 60 * 60 * 24 * 7; // 7 дней
    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;

    SecretKey getSigningKey() {
        return Jwts.SIG.HS256.key().build();
    }

    public String generateAccessToken(String email) {
        return Jwts.builder()
                .subject(email)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + ACCESS_TOKEN_EXPIRATION))
                .signWith(getSigningKey())
                .compact();
    }

    public String extractEmail(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public String generateRefreshToken(String email) {
        User assignedUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException(email, "email"));

        return refreshTokenRepository.findByOwner(assignedUser).map(refreshToken -> {
            refreshToken.setExpiration(Instant.now().plusMillis(REFRESH_TOKEN_EXPIRATION));
            refreshToken.setToken(UUID.randomUUID().toString());

            return refreshTokenRepository.saveAndFlush(refreshToken).getToken();
        }).orElseGet(() -> refreshTokenRepository
                .save(RefreshToken.builder()
                        .owner(assignedUser)
                        .token(UUID.randomUUID().toString())
                        .expiration(Instant.now().plusMillis(REFRESH_TOKEN_EXPIRATION))
                        .build())
                .getToken());
    }
}
