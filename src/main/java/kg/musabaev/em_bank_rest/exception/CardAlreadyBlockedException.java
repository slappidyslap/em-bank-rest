package kg.musabaev.em_bank_rest.exception;

import org.springframework.http.HttpStatus;

public class CardAlreadyBlockedException extends AbstractHttpStatusException {
    @Override
    public HttpStatus httpStatus() {
        return HttpStatus.CONFLICT;
    }

    public CardAlreadyBlockedException() {
        super("couldn't block already blocked card");
    }
}
