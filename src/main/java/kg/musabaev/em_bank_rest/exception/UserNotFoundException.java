package kg.musabaev.em_bank_rest.exception;

public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(Long id) {
        super("User by " + id + " not found");
    }
}
