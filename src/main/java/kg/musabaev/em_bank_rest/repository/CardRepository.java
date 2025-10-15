package kg.musabaev.em_bank_rest.repository;

import kg.musabaev.em_bank_rest.entity.Card;
import kg.musabaev.em_bank_rest.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface CardRepository extends JpaRepository<Card, Long>, JpaSpecificationExecutor<Card> {

    Page<Card> findAllByUser(User user, Pageable pageable);

    Optional<Card> findByNumber(String number);
}
