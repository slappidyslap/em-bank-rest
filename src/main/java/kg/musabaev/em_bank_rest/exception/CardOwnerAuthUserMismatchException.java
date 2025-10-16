package kg.musabaev.em_bank_rest.exception;

public class CardOwnerAuthUserMismatchException extends RuntimeException {
    public CardOwnerAuthUserMismatchException() {
        super("Card owner does not equal authenticated user");
    }
}
