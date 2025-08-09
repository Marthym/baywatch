package fr.ght1pc9kc.baywatch.notify.domain.exceptions;

public class NotifyModuleException extends RuntimeException {
    public NotifyModuleException(String message) {
        super(message);
    }

    public NotifyModuleException(String message, Throwable cause) {
        super(message, cause);
    }
}
