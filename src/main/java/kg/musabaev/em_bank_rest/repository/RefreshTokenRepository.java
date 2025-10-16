package kg.musabaev.em_bank_rest.repository;

import kg.musabaev.em_bank_rest.entity.User;
import kg.musabaev.em_bank_rest.security.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByOwner(User owner);

    Optional<RefreshToken> findByToken(String token);

    @Query("SELECT rt.owner.email FROM RefreshToken rt WHERE rt.token = ?1")
    Optional<String> findRefreshTokenOwnerEmailByToken(String token);
}