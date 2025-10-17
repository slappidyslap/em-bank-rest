package kg.musabaev.em_bank_rest.exception;

import kg.musabaev.em_bank_rest.util.Pair;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mapping.PropertyReferenceException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;

import java.util.List;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AbstractHttpStatusException.class)
    ResponseEntity<Pair<String>> handleFieldNotValid(AbstractHttpStatusException ex) {
        return response(ex.getMessage(), ex.httpStatus());
    }

    @ExceptionHandler(PropertyReferenceException.class)
    ResponseEntity<Pair<String>> handlePropertyReference(PropertyReferenceException ex) {
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
