package kg.musabaev.em_bank_rest.exception;

import org.springframework.http.HttpStatus;

public class UserNotFoundException extends AbstractHttpStatusException {

    @Override
    public HttpStatus httpStatus() {
        return HttpStatus.NOT_FOUND;
    }
    public UserNotFoundException(Long id) {
        super("User by " + id + " id not found");
    }

    public UserNotFoundException(String byValue, String byWhat) {
        super("User by " + byValue + " " + byWhat + " not found");
    }
}
