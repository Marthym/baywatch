package fr.ght1pc9kc.baywatch.security.domain.exceptions;

public class SecurityException extends RuntimeException {
    public SecurityException(String message, Throwable cause) {
        super(message, cause);
    }
    public SecurityException(String message) {
        super(message);
    }
}
