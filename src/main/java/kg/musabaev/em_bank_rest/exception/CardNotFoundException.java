package kg.musabaev.em_bank_rest.exception;

import org.springframework.http.HttpStatus;

public class CardNotFoundException extends AbstractHttpStatusException {
    @Override
    public HttpStatus httpStatus() {
        return HttpStatus.NOT_FOUND;
    }
    public CardNotFoundException(Long id) {
        super("Card by " + id + " id not found");
    }

    public CardNotFoundException(String cardNumber) {
        super("Card by " + cardNumber + " number not found");
    }
}
