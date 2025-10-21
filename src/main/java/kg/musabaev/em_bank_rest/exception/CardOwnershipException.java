package kg.musabaev.em_bank_rest.exception;

import org.springframework.http.HttpStatus;

public class CardOwnershipException extends AbstractHttpStatusException {
    @Override
    public HttpStatus httpStatus() {
        return HttpStatus.BAD_REQUEST;
    }
    public CardOwnershipException(String message) {
        super(message);
    }
}
