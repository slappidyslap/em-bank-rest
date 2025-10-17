package kg.musabaev.em_bank_rest.exception;

import org.springframework.http.HttpStatus;

public class CardUnsupportedOperationException extends AbstractHttpStatusException {
    @Override
    public HttpStatus httpStatus() {
        return HttpStatus.CONFLICT;
    }

    public CardUnsupportedOperationException(String message) {
        super(message);
    }
}
