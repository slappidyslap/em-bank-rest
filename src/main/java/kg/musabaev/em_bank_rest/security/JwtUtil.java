package kg.musabaev.em_bank_rest.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import kg.musabaev.em_bank_rest.entity.User;
import kg.musabaev.em_bank_rest.exception.UserNotFoundException;
import kg.musabaev.em_bank_rest.repository.RefreshTokenRepository;
import kg.musabaev.em_bank_rest.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtUtil {

    @Value("${app.jwt-secret}")
    private String SECRET;
    @Value("${app.access-token-expiration}")
    private Long ACCESS_TOKEN_EXPIRATION;
    @Value("${app.refresh-token-expiration}")
    private Long REFRESH_TOKEN_EXPIRATION;

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(SECRET));
    }

    public String generateAccessToken(String email) {
        return Jwts.builder()
                .subject(email)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + ACCESS_TOKEN_EXPIRATION))
                .signWith(getSigningKey(), Jwts.SIG.HS256)
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
            log.error(e.getMessage());
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

    public String getAccessTokenFromRequest(HttpServletRequest request) {
        String authorization = request.getHeader(HttpHeaders.AUTHORIZATION);
        return authorization != null && authorization.startsWith("Bearer") ? authorization.substring(7) : null;
    }
}
