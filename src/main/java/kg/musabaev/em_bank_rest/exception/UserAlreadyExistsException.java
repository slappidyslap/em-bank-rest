package kg.musabaev.em_bank_rest.exception;

import org.springframework.http.HttpStatus;

public class UserAlreadyExistsException extends AbstractHttpStatusException {
    @Override
    public HttpStatus httpStatus() {
        return HttpStatus.CONFLICT;
    }
    public UserAlreadyExistsException() {
        super("User with given email already exists");
    }
}
