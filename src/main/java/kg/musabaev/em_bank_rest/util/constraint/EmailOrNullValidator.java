package kg.musabaev.em_bank_rest.util.constraint;

import jakarta.validation.ConstraintValidatorContext;
import org.hibernate.validator.internal.constraintvalidators.bv.EmailValidator;

/**
 * @see org.hibernate.validator.internal.constraintvalidators.bv.EmailValidator
 */
public class EmailOrNullValidator extends EmailValidator {
    @Override
    public boolean isValid(CharSequence value, ConstraintValidatorContext context) {
        if (value == null) return true;
        return super.isValid(value, context);
    }
}
