package fr.ght1pc9kc.baywatch.domain.exceptions;

public class BadCriteriaFilter extends RuntimeException {
    public BadCriteriaFilter() {
    }

    public BadCriteriaFilter(String message) {
        super(message);
    }

    public BadCriteriaFilter(String message, Throwable cause) {
        super(message, cause);
    }
}
