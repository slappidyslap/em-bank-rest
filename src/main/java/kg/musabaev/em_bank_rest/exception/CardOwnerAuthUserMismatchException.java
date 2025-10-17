package kg.musabaev.em_bank_rest.exception;

import org.springframework.http.HttpStatus;

public class CardOwnerAuthUserMismatchException extends AbstractHttpStatusException {
    @Override
    public HttpStatus httpStatus() {
        return HttpStatus.BAD_REQUEST;
    }
    public CardOwnerAuthUserMismatchException() {
        super("Card owner does not equal authenticated user");
    }
}
