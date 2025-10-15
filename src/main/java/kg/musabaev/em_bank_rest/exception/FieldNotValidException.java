package kg.musabaev.em_bank_rest.exception;

public class FieldNotValidException extends RuntimeException {
    public FieldNotValidException(String message) {
        super(message);
    }
}
