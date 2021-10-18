package fr.ght1pc9kc.baywatch.infra.security.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.NO_CONTENT)
public final class NoSessionException extends RuntimeException {
    public NoSessionException(String message) {
        super(message);
    }
}
