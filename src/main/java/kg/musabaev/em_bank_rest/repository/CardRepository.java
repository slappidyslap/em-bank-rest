package kg.musabaev.em_bank_rest.repository;

import kg.musabaev.em_bank_rest.entity.Card;
import kg.musabaev.em_bank_rest.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CardRepository extends JpaRepository<Card, Long> {

    Page<Card> findAllByOwner(User owner, Pageable pageable);

    Optional<Card> findByNumber(String cardNumber);
}
