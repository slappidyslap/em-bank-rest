package kg.musabaev.em_bank_rest.exception;

import org.springframework.http.HttpStatus;

public class SelfTransferNotAllowedException extends AbstractHttpStatusException {
    @Override
    public HttpStatus httpStatus() {
        return HttpStatus.BAD_REQUEST;
    }
    public SelfTransferNotAllowedException() {
        super("Source and destination cards must be different");
    }
}
