package kg.musabaev.em_bank_rest.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler({CardNotFoundException.class, UserNotFoundException.class})
    @ResponseStatus(HttpStatus.NOT_FOUND)
    void handleResourceNotFound() {
    }

    @ExceptionHandler(FieldNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    void handleFieldNotValid() {// TODO кастомный респонсбади с смс ошибкой
    }
}
