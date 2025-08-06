package fr.ght1pc9kc.baywatch.notify.domain.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class SmtpException extends RuntimeException {

    private final int status;

    public SmtpException(Throwable cause) {
        super(cause);
        this.status = -1;
    }

    public SmtpException(int status, String message) {
        super(message);
        this.status = status;
    }
}
