package fr.ght1pc9kc.baywatch.infra.security.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.UNAUTHORIZED)
public class BaywatchCredentialsException extends RuntimeException {
    public BaywatchCredentialsException(Throwable cause) {
        super(cause);
    }

    public BaywatchCredentialsException(String message) {
        super(message);
    }
}
