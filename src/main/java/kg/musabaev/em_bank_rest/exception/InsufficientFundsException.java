package kg.musabaev.em_bank_rest.exception;

import org.springframework.http.HttpStatus;

public class InsufficientFundsException extends AbstractHttpStatusException {
    @Override
    public HttpStatus httpStatus() {
        return HttpStatus.BAD_REQUEST;
    }
    public InsufficientFundsException() {
        super("Insufficient funds");
    }
}
