package kg.musabaev.em_bank_rest.exception;

import org.springframework.http.HttpStatus;

public class FieldNotValidException extends AbstractHttpStatusException {
    @Override
    public HttpStatus httpStatus() {
        return HttpStatus.BAD_REQUEST;
    }
    public FieldNotValidException(String message) {
        super(message);
    }
}
