package fr.ght1pc9kc.baywatch.common.infra.validators;

import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

class NullOrNotBlankValidatorTest {

    private final NullOrNotBlankValidator validator = new NullOrNotBlankValidator();

    @ParameterizedTest
    @NullSource
    void isValid_shouldReturnTrue_whenValueIsNull(CharSequence value) {
        ConstraintValidatorContext context = null;

        boolean result = validator.isValid(value, context);

        assertTrue(result);
    }

    @ParameterizedTest
    @ValueSource(strings = {"", " ", "   ", "\t", "\n", "\r\n", " \t \n "})
    void isValid_shouldReturnFalse_whenValueIsBlankAfterTrim(String value) {
        ConstraintValidatorContext context = null;

        boolean result = validator.isValid(value, context);

        assertFalse(result);
    }

    @ParameterizedTest
    @ValueSource(strings = {"a", " a ", "0", "x_y", "bonjour", "  bonjour  "})
    void isValid_shouldReturnTrue_whenValueHasNonWhitespaceContent(String value) {
        ConstraintValidatorContext context = null;

        boolean result = validator.isValid(value, context);

        assertTrue(result);
    }

    @Test
    void isValid_shouldWorkWithNonStringCharSequence_likeStringBuilder() {
        ConstraintValidatorContext context = null;

        assertFalse(validator.isValid(new StringBuilder("   "), context));
        assertTrue(validator.isValid(new StringBuilder("  ok  "), context));
    }
}