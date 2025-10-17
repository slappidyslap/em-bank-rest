package kg.musabaev.em_bank_rest.exception;

import org.springframework.http.HttpStatus;

public class CardBlockRequestNotFoundException extends AbstractHttpStatusException {
    @Override
    public HttpStatus httpStatus() {
        return HttpStatus.NOT_FOUND;
    }
    public CardBlockRequestNotFoundException(Long cardId) {
        super("Card with " + cardId + " ID was not requested to block");
    }
}
