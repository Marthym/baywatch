package fr.ght1pc9kc.baywatch.common.api.exceptions;

public class UnauthorizedException extends RuntimeException {
    public static final String AUTHENTICATION_NOT_FOUND = "Authentication not found !";

    public UnauthorizedException() {
        super(AUTHENTICATION_NOT_FOUND);
    }

    public UnauthorizedException(String message) {
        super(message);
    }

    public UnauthorizedException(String message, Throwable cause) {
        super(message, cause);
    }

    public UnauthorizedException(Throwable cause) {
        super(AUTHENTICATION_NOT_FOUND, cause);
    }
}
