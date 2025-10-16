package kg.musabaev.em_bank_rest.exception;

public class RefreshTokenExpiredException extends RuntimeException {
    public RefreshTokenExpiredException() {
        super("Given refresh token expired");
    }
}
