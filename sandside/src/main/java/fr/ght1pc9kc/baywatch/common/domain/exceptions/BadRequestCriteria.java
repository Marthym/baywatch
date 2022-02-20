package fr.ght1pc9kc.baywatch.common.domain.exceptions;

public class BadRequestCriteria extends RuntimeException {
    public BadRequestCriteria() {
    }

    public BadRequestCriteria(String message) {
        super(message);
    }

    public BadRequestCriteria(String message, Throwable cause) {
        super(message, cause);
    }
}
