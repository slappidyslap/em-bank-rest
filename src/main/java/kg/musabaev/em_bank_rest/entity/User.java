package kg.musabaev.em_bank_rest.entity;

import jakarta.persistence.*;
import kg.musabaev.em_bank_rest.security.Role;
import lombok.*;
import org.hibernate.annotations.NaturalId;

import java.util.Set;

@Getter
@Setter
@EqualsAndHashCode(of = "email")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String fullName;

    @Column(nullable = false, unique = true)
    @NaturalId(mutable = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Role role;

    @Column(nullable = false)
    @OneToMany(mappedBy = "user", cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    private Set<Card> cards;

}
