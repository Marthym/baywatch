package fr.ght1pc9kc.baywatch.infra.security.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.UNAUTHORIZED)
public class BadCredentialException extends RuntimeException {
    public BadCredentialException(Throwable cause) {
        super(cause);
    }
}
