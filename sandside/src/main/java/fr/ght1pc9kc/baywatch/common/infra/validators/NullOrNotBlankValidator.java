package fr.ght1pc9kc.baywatch.common.infra.validators;

import fr.ght1pc9kc.baywatch.common.api.constraints.NullOrNotBlank;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.context.i18n.LocaleContextHolder;

public final class NullOrNotBlankValidator implements ConstraintValidator<NullOrNotBlank, CharSequence> {

    @Override
    public boolean isValid(CharSequence value, ConstraintValidatorContext context) {
        System.out.println("Locale = " + LocaleContextHolder.getLocale());
        if (value == null) return true;
        return !value.toString().trim().isEmpty();
    }
}
