package fr.ght1pc9kc.baywatch.security.infra.exceptions;

import fr.ght1pc9kc.baywatch.common.api.exceptions.TranslatableException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.UNAUTHORIZED)
public class BaywatchCredentialsException extends SecurityException implements TranslatableException {
    public BaywatchCredentialsException(Throwable cause) {
        super(cause);
    }

    public BaywatchCredentialsException(String message) {
        super(message);
    }

    @Override
    public String getTranslationKey() {
        return "security.exception.bad.credentials";
    }

    @Override
    public String classification() {
        return "UNAUTHORIZED";
    }
}
