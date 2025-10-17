package kg.musabaev.em_bank_rest.exception;

public class CardBlockRequestNotFoundException extends RuntimeException {
    public CardBlockRequestNotFoundException(Long cardId) {
        super("Card with " + cardId + " ID was not requested to block");
    }
}
