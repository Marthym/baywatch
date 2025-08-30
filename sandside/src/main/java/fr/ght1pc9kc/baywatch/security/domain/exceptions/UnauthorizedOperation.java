package fr.ght1pc9kc.baywatch.security.domain.exceptions;

import fr.ght1pc9kc.baywatch.common.api.exceptions.TranslatableException;

public class UnauthorizedOperation extends SecurityException implements TranslatableException {
    public UnauthorizedOperation(String message, Throwable cause) {
        super(message, cause);
    }

    public UnauthorizedOperation(String message) {
        super(message);
    }

    @Override
    public String getTranslationKey() {
        return "security.exception.unauthorized.operation";
    }

    @Override
    public String classification() {
        return "UNAUTHORIZED";
    }
}
