package kg.musabaev.em_bank_rest.entity;

import jakarta.persistence.*;
import kg.musabaev.em_bank_rest.util.CardNumberConverter;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;


@Getter
@Setter
@EqualsAndHashCode(of = {"number", "expiry", "user"})
@Entity
@Table(name = "cards")
public class Card {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 512)
    @Convert(converter = CardNumberConverter.class)
    private String number;

    @Column(nullable = false)
    private LocalDate expiry;

    @Column(nullable = false)
    private String owner;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private CardStatus status;

    // fixme double
    @Column(nullable = false)
    private Double balance;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
}
