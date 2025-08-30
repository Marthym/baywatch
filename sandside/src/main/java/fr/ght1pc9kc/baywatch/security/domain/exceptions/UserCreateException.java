package fr.ght1pc9kc.baywatch.security.domain.exceptions;

import fr.ght1pc9kc.baywatch.common.api.exceptions.TranslatableException;
import lombok.Getter;

import java.util.List;
import java.util.Map;

@Getter
public class UserCreateException extends SecurityException implements TranslatableException {
    private final List<String> fields;

    public UserCreateException(String message, List<String> fields, Throwable cause) {
        super(message, cause);
        this.fields = fields;
    }

    public UserCreateException(String message, List<String> fields) {
        super(message);
        this.fields = fields;
    }

    @Override
    public Map<String, Object> getExtensions() {
        return Map.of("properties", this.fields);
    }

    @Override
    public String getTranslationKey() {
        return "security.exception.user.create.error";
    }

    @Override
    public String classification() {
        return "BAD_REQUEST";
    }
}
