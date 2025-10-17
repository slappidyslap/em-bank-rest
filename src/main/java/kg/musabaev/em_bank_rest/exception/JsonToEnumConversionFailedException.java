package kg.musabaev.em_bank_rest.exception;

import org.springframework.http.HttpStatus;

import java.util.Arrays;
import java.util.stream.Collectors;

public class JsonToEnumConversionFailedException extends AbstractHttpStatusException {

  public JsonToEnumConversionFailedException(String value, Class<? extends Enum<?>> enumClass) {
    super(String.format("Unexpected enum value %s. Expected values for type %s are [%s]",
            value,
            enumClass.getSimpleName(),
            Arrays.stream(enumClass.getEnumConstants()).map(Object::toString).collect(Collectors.joining(", "))));
  }

  @Override
  public HttpStatus httpStatus() {
    return HttpStatus.BAD_REQUEST;
  }
}
