package kg.musabaev.em_bank_rest.exception;

import org.springframework.http.HttpStatus;

public class CardExpiredException extends AbstractHttpStatusException {
    @Override
    public HttpStatus httpStatus() {
        return HttpStatus.BAD_REQUEST;
    }
    public CardExpiredException() {
        super("Given card expired");
    }
}
