package kg.musabaev.em_bank_rest.exception;

public class CardNotFoundException extends RuntimeException {
    public CardNotFoundException(Long id) {
        super("Card by " + id + " not found");
    }
}
