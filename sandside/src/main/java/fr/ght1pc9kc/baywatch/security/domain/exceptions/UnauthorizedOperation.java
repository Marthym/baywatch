package fr.ght1pc9kc.baywatch.security.domain.exceptions;

public class UnauthorizedOperation extends SecurityException {
    public UnauthorizedOperation(String message, Throwable cause) {
        super(message, cause);
    }

    public UnauthorizedOperation(String message) {
        super(message);
    }
}
