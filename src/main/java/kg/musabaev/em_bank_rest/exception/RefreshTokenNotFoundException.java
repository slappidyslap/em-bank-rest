package kg.musabaev.em_bank_rest.exception;

import org.springframework.http.HttpStatus;

public class RefreshTokenNotFoundException extends AbstractHttpStatusException {
    @Override
    public HttpStatus httpStatus() {
        return HttpStatus.NOT_FOUND;
    }
    public RefreshTokenNotFoundException(String token) {
        super("Refresh token by " + token + " token not found");
    }
}
