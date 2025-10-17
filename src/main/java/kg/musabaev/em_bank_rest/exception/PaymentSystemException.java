package kg.musabaev.em_bank_rest.exception;

import org.springframework.http.HttpStatus;

public class PaymentSystemException extends AbstractHttpStatusException {
    @Override
    public HttpStatus httpStatus() {
        return HttpStatus.INTERNAL_SERVER_ERROR;
    }

    public PaymentSystemException(String message) {
        super(message);
    }
}
