package kg.musabaev.em_bank_rest.repository;

import kg.musabaev.em_bank_rest.entity.CardBlockRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CardBlockRequestRepository extends JpaRepository<CardBlockRequest, Long> {
}