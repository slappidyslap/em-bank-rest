package kg.musabaev.em_bank_rest.exception;

import kg.musabaev.em_bank_rest.util.Pair;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;

import java.util.List;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AbstractHttpStatusException.class)
    ResponseEntity<Pair<String>> handleFieldNotValid(AbstractHttpStatusException ex) {
        return response(ex.getMessage(), ex.httpStatus());
    }

    @ExceptionHandler(CardNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    ResponseEntity<?> handleCardNotFound(CardNotFoundException ex) {
        return response(ex.getMessage(), ex.httpStatus());
    }

    @ExceptionHandler(CardBlockRequestNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    ResponseEntity<?> handleCardBlockRequestNotFound(CardBlockRequestNotFoundException ex) {
        return response(ex.getMessage(), ex.httpStatus());
    }

    @ExceptionHandler(UserNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    ResponseEntity<?> handleUserNotFound(UserNotFoundException ex) {
        return response(ex.getMessage(), ex.httpStatus());
    }

    @ExceptionHandler(RefreshTokenNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    ResponseEntity<?> handleRefreshTokenNotFound(RefreshTokenNotFoundException ex) {
        return response(ex.getMessage(), ex.httpStatus());
    }

    // --- Исключения, связанные с "ПЛОХИМ ЗАПРОСОМ" (400) ---

    @ExceptionHandler(FieldNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    ResponseEntity<?> handleFieldNotValid(FieldNotValidException ex) {
        // Часто используется для ошибок валидации (например, @Valid)
        return response(ex.getMessage(), ex.httpStatus());
    }

    @ExceptionHandler(CardExpiredException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    ResponseEntity<?> handleCardExpired(CardExpiredException ex) {
        return response(ex.getMessage(), ex.httpStatus());
    }

    @ExceptionHandler(InactiveCardException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    ResponseEntity<?> handleInactiveCard(InactiveCardException ex) {
        return response(ex.getMessage(), ex.httpStatus());
    }

    @ExceptionHandler(InsufficientFundsException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    ResponseEntity<?> handleInsufficientFunds(InsufficientFundsException ex) {
        return response(ex.getMessage(), ex.httpStatus());
    }

    @ExceptionHandler(CardOwnershipException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    ResponseEntity<?> handleCardOwnership(CardOwnershipException ex) {
        // Например, попытка выполнить операцию с чужой картой
        return response(ex.getMessage(), ex.httpStatus());
    }

    @ExceptionHandler(CardOwnerAuthUserMismatchException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    ResponseEntity<?> handleCardOwnerAuthUserMismatch(CardOwnerAuthUserMismatchException ex) {
        return response(ex.getMessage(), ex.httpStatus());
    }

    @ExceptionHandler(SelfTransferNotAllowedException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    ResponseEntity<?> handleSelfTransferNotAllowed(SelfTransferNotAllowedException ex) {
        return response(ex.getMessage(), ex.httpStatus());
    }

    @ExceptionHandler(UserAlreadyExistsException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    ResponseEntity<?> handleUserAlreadyExists(UserAlreadyExistsException ex) {
        return response(ex.getMessage(), ex.httpStatus());
    }

    // --- Исключения, связанные с "ЗАПРЕЩЕНО" (403) или "НЕПОДДЕРЖИВАЕМАЯ ОПЕРАЦИЯ" (409) ---

    @ExceptionHandler(CardUnsupportedOperationException.class)
    @ResponseStatus(HttpStatus.CONFLICT) // 409: Conflict (логический конфликт операции)
    ResponseEntity<?> handleCardUnsupportedOperation(CardUnsupportedOperationException ex) {
        return response(ex.getMessage(), ex.httpStatus());
    }

    @ExceptionHandler(RefreshTokenExpiredException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED) // 401: Unauthorized (токен недействителен, требуется новый)
    ResponseEntity<?> handleRefreshTokenExpired(RefreshTokenExpiredException ex) {
        return response(ex.getMessage(), ex.httpStatus());
    }

    // --- Исключения, связанные с ВНЕШНЕЙ СИСТЕМОЙ (503 или 500) ---

    @ExceptionHandler(PaymentSystemException.class)
    @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE) // 503: Service Unavailable (проблема с внешней зависимостью)
    ResponseEntity<?> handlePaymentSystem(PaymentSystemException ex) {
        return response(ex.getMessage(), ex.httpStatus());
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
                        log.error(e.getMessage());
                        return "some field " + m.getDefaultMessage();
                    }
                })
                .toList();
        return response(messages, BAD_REQUEST);
    }

    private ResponseEntity<Pair<List<String>>> response(List<String> msg, HttpStatus status) {
        return ResponseEntity.status(status.value()).body(Pair.of("errors", msg));
    }

    private ResponseEntity<Pair<String>> response(String msg, HttpStatus status) {
        return ResponseEntity.status(status.value()).body(Pair.of("error", msg));
    }
}
