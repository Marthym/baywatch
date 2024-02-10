package fr.ght1pc9kc.baywatch.security.domain.exceptions;

import lombok.Getter;

@Getter
public class ConstraintViolationPersistenceException extends RuntimeException {
    private final String propertyField;

    public ConstraintViolationPersistenceException(String propertyField, Throwable cause) {
        super(cause.getLocalizedMessage(), cause);
        this.propertyField = propertyField;
    }
}
