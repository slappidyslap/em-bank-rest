package kg.musabaev.em_bank_rest.exception;

public class CardOwnershipException extends RuntimeException {
    public CardOwnershipException() {
        super("Both cards must belong to one user");
    }
}
