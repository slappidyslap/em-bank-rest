package kg.musabaev.em_bank_rest.exception;

import org.springframework.http.HttpStatus;

public abstract class AbstractHttpStatusException extends RuntimeException {
    public abstract HttpStatus httpStatus();

    public AbstractHttpStatusException(String message) {
        super(message);
    }

    public AbstractHttpStatusException(String message, Throwable cause) {
        super(message, cause);
    }
}
