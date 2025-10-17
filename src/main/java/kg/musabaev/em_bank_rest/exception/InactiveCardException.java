package kg.musabaev.em_bank_rest.exception;

import org.springframework.http.HttpStatus;

public class InactiveCardException extends AbstractHttpStatusException {
    @Override
    public HttpStatus httpStatus() {
        return HttpStatus.BAD_REQUEST;
    }
    public InactiveCardException() {
        super("Source card must be active");
    }
}
