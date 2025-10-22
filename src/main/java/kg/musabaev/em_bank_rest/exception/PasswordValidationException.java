package kg.musabaev.em_bank_rest.exception;

import org.springframework.http.HttpStatus;

public class PasswordValidationException extends AbstractHttpStatusException {
    @Override
    public HttpStatus httpStatus() {
        return HttpStatus.BAD_REQUEST;
    }

    public PasswordValidationException(String s) {
        super(s);
    }
}
