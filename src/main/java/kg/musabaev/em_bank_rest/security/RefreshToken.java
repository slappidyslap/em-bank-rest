package kg.musabaev.em_bank_rest.security;


import jakarta.persistence.*;
import kg.musabaev.em_bank_rest.entity.User;
import lombok.*;

import java.time.Instant;

@Getter
@Setter
@EqualsAndHashCode(of = "token")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "refresh_tokens")
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false, updatable = false)
    private User owner;

    @Column(nullable = false, unique = true)
    private String token;

    @Column(nullable = false)
    private Instant expiration;
}
