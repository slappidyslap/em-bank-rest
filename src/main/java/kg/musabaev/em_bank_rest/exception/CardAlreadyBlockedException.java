package kg.musabaev.em_bank_rest.exception;

public class CardAlreadyBlockedException extends RuntimeException {
    public CardAlreadyBlockedException() {
        super("couldn't block already blocked card");
    }
}
