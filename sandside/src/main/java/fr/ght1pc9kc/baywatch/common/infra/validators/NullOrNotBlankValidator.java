package fr.ght1pc9kc.baywatch.common.infra.validators;

import fr.ght1pc9kc.baywatch.common.api.constraints.NullOrNotBlank;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public final class NullOrNotBlankValidator implements ConstraintValidator<NullOrNotBlank, CharSequence> {

    @Override
    public boolean isValid(CharSequence value, ConstraintValidatorContext context) {
        if (value == null) return true;
        return !value.toString().trim().isEmpty();
    }
}
