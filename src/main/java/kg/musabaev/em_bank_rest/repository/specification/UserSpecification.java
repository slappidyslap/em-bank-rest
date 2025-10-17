package kg.musabaev.em_bank_rest.repository.specification;

import kg.musabaev.em_bank_rest.entity.User;
import kg.musabaev.em_bank_rest.exception.JsonToEnumConversionFailedException;
import kg.musabaev.em_bank_rest.security.Role;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.util.Locale;

public record UserSpecification(
        String email,
        String fullName,
        Long id,
        String role) {

    public Specification<User> build() {
        try {
            Role.valueOf(role.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw new JsonToEnumConversionFailedException(role, Role.class);
        }
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
        return ((root, query, cb) -> StringUtils.hasText(role)
                ? cb.equal(root.get("role"), role)
                : null);
    }
}