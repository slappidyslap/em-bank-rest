package kg.musabaev.em_bank_rest.repository.specification;

import kg.musabaev.em_bank_rest.entity.Card;
import kg.musabaev.em_bank_rest.entity.CardStatus;
import kg.musabaev.em_bank_rest.exception.FieldNotValidException;
import org.springframework.data.jpa.domain.Specification;

import java.util.Arrays;
import java.util.Locale;

public record CardSpecification(
        String status,
        Long userId
) {

    public Specification<Card> build() {
        CardStatus enumStatus;
        try {
            enumStatus = CardStatus.valueOf(status.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw new FieldNotValidException(
                    "Invalid card status provided. Status must be one of: " + Arrays.toString(CardStatus.values()));
        }
        return withStatus(enumStatus).and(withUserId(userId));
    }

    private static Specification<Card> withStatus(CardStatus status) {
        return (root, query, criteriaBuilder) -> status == null
                ? criteriaBuilder.conjunction()
                : criteriaBuilder.equal(root.get("status"), status);
    }

    private static Specification<Card> withUserId(Long userId) {
        return (root, query, criteriaBuilder) -> userId == null
                ? criteriaBuilder.conjunction()
                : criteriaBuilder.equal(root.get("user").get("id"), userId);
    }
}
