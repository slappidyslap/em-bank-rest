package kg.musabaev.em_bank_rest.exception;

import kg.musabaev.em_bank_rest.util.Pair;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;

import java.util.List;

import static org.springframework.http.HttpStatus.*;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler({CardNotFoundException.class, UserNotFoundException.class, RefreshTokenNotFoundException.class})
    ResponseEntity<Pair<String>> handleResourceNotFound(Exception ex) {
        return response(ex.getMessage(), NOT_FOUND);
    }

    @ExceptionHandler({
            FieldNotValidException.class,
            CardAlreadyBlockedException.class,
            CardOwnershipException.class,
            InactiveCardException.class,
            InsufficientFundsException.class,
            SelfTransferNotAllowedException.class
    })
    @ResponseStatus(BAD_REQUEST)
    ResponseEntity<Pair<String>> handleFieldNotValid(FieldNotValidException ex) {
        return response(ex.getMessage(), BAD_REQUEST);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Pair<List<String>>> handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
        List<String> messages = ex
                .getBindingResult()
                .getFieldErrors()
                .stream()
                .map(fieldError -> fieldError.getField() + " " + fieldError.getDefaultMessage())
                .toList();
        return response(messages, BAD_REQUEST);

    }

    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<Pair<List<String>>> handleHandlerMethodValidation(HandlerMethodValidationException ex) {
        List<String> messages = ex.getParameterValidationResults()
                .stream()
                .flatMap(r -> r.getResolvableErrors().stream())
                .map(m -> {
                    try {
                        var fieldName = m.getCodes()[1].split("\\.")[1];
                        return fieldName + " " + m.getDefaultMessage();
                    } catch (Exception e) {
                        return "some field " + m.getDefaultMessage();
                    }
                })
                .toList();
        return response(messages, BAD_REQUEST);
    }

    @ExceptionHandler(UserAlreadyExistsException.class)
    @ResponseStatus(CONFLICT)
    public void handleUserAlreadyExists() {
    }

    @ExceptionHandler(RefreshTokenExpiredException.class)
    @ResponseStatus(UNAUTHORIZED)
    public void handleRefreshTokenExpired() {
    }

    @ExceptionHandler(CardOwnerAuthUserMismatchException.class)
    @ResponseStatus(UNAUTHORIZED)
    public void handleCardOwnerAuthUserMismatch() {
    }

    private ResponseEntity<Pair<List<String>>> response(List<String> msg, HttpStatus status) {
        return ResponseEntity.status(status.value()).body(Pair.of("errors", msg));
    }

    private ResponseEntity<Pair<String>> response(String msg, HttpStatus status) {
        return ResponseEntity.status(status.value()).body(Pair.of("errors", msg));
    }
}
