package fr.ght1pc9kc.baywatch.security.domain.exceptions;

import lombok.Getter;

import java.util.List;

@Getter
public class UserCreateException extends SecurityException {
    private final List<String> fields;

    public UserCreateException(String message, List<String> fields, Throwable cause) {
        super(message, cause);
        this.fields = fields;
    }

    public UserCreateException(String message, List<String> fields) {
        super(message);
        this.fields = fields;
    }
}
