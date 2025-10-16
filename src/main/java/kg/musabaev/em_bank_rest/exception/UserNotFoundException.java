package kg.musabaev.em_bank_rest.exception;

public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(Long id) {
        super("User by " + id + " id not found");
    }

    public UserNotFoundException(String byValue, String byWhat) {
        super("User by " + byValue + " " + byWhat + " not found");
    }
}
