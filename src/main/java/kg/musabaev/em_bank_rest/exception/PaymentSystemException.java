package kg.musabaev.em_bank_rest.exception;

import org.springframework.http.HttpStatus;

public class PaymentSystemException extends AbstractHttpStatusException {
    @Override
    public HttpStatus httpStatus() {
        return HttpStatus.SERVICE_UNAVAILABLE;
    }

    public PaymentSystemException(String message) {
        super(message);
    }
}
