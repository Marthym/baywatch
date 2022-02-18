package fr.ght1pc9kc.baywatch.domain.security.exceptions;

public class UnauthenticatedUser extends SecurityException {

    public UnauthenticatedUser(String message) {
        super(message);
    }

    public UnauthenticatedUser(String message, Throwable cause) {
        super(message, cause);
    }
}
