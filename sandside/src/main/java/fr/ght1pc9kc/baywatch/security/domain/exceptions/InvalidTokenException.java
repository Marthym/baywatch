package fr.ght1pc9kc.baywatch.security.domain.exceptions;

import fr.ght1pc9kc.baywatch.common.api.exceptions.TranslatableException;

public class InvalidTokenException extends SecurityException implements TranslatableException {
    public InvalidTokenException(String message) {
        super(message);
    }

    @Override
    public String getTranslationKey() {
        return "security.exception.invalid.token";
    }

    @Override
    public String classification() {
        return "UNAUTHORIZED";
    }
}
