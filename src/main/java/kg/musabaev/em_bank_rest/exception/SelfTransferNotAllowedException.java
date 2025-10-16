package kg.musabaev.em_bank_rest.exception;

public class SelfTransferNotAllowedException extends RuntimeException {
    public SelfTransferNotAllowedException() {
        super("Source and destination cards must be different");
    }
}
