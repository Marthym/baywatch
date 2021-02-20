package fr.ght1pc9kc.baywatch.domain.exceptions;

public class SecurityException extends RuntimeException {
    public SecurityException(String message, Throwable cause) {
        super(message, cause);
    }
}
