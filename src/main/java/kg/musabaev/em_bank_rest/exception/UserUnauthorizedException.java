package kg.musabaev.em_bank_rest.exception;

public class UserUnauthorizedException extends RuntimeException {
    public UserUnauthorizedException() {
        super("Incorrect credentials");
    }
}
