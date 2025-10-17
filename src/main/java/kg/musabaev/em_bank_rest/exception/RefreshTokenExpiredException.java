package kg.musabaev.em_bank_rest.exception;

import org.springframework.http.HttpStatus;

public class RefreshTokenExpiredException extends AbstractHttpStatusException {
    @Override
    public HttpStatus httpStatus() {
        return HttpStatus.UNAUTHORIZED;
    }
    public RefreshTokenExpiredException() {
        super("Given refresh token expired");
    }
}
