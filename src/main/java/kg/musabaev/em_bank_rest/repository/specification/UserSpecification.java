package kg.musabaev.em_bank_rest.repository.specification;

import kg.musabaev.em_bank_rest.entity.User;
import kg.musabaev.em_bank_rest.security.Role;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

public record UserSpecification(
        String email, // TOdo validation
        String fullName,
        Long id,
        Role role) {

    public Specification<User> build() {
        return withEmail()
                .and(withFullName())
                .and(withId())
                .and(withRole());
    }

    private Specification<User> withEmail() {
        return ((root, query, cb) -> StringUtils.hasText(email)
                ? cb.equal(root.get("email"), email)
                : null);
    }

    private Specification<User> withFullName() {
        return ((root, query, cb) -> StringUtils.hasText(fullName)
                ? cb.equal(root.get("fullName"), fullName)
                : null);
    }

    private Specification<User> withId() {
        return ((root, query, cb) -> id != null
                ? cb.equal(root.get("id"), id)
                : null);
    }

    private Specification<User> withRole() {
        return ((root, query, cb) -> role != null
                ? cb.equal(root.get("role"), role)
                : null);
    }
}