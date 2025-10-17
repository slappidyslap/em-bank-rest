package kg.musabaev.em_bank_rest.repository.specification;

import kg.musabaev.em_bank_rest.entity.Card;
import kg.musabaev.em_bank_rest.entity.CardStatus;
import kg.musabaev.em_bank_rest.exception.JsonToEnumConversionFailedException;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.util.Locale;

public record CardSpecification(
        String status,
        Long userId
) {

    public Specification<Card> build() {
        try {
            CardStatus.valueOf(status.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw new JsonToEnumConversionFailedException(status, CardStatus.class);
        }
        return withStatus().and(withUserId());
    }

    private Specification<Card> withStatus() {
        return (root, query, criteriaBuilder) -> StringUtils.hasText(status)
                ? criteriaBuilder.conjunction()
                : criteriaBuilder.equal(root.get("status"), status);
    }

    private Specification<Card> withUserId() {
        return (root, query, criteriaBuilder) -> userId == null
                ? criteriaBuilder.conjunction()
                : criteriaBuilder.equal(root.get("user").get("id"), userId);
    }
}
