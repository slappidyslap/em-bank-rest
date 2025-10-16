package kg.musabaev.em_bank_rest.exception;

public class InactiveCardException extends RuntimeException {
    public InactiveCardException() {
        super("Source card must be active");
    }
}
