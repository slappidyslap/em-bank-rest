package kg.musabaev.em_bank_rest.exception;

public class RefreshTokenNotFoundException extends RuntimeException {
    public RefreshTokenNotFoundException(String token) {
        super("Refresh token by " + token + " token not found");
    }
}
