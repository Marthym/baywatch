package fr.ght1pc9kc.baywatch.notify.domain.exceptions;

import lombok.Getter;

@Getter
public class SmtpException extends NotifyModuleException {

    private final int status;

    public SmtpException(Throwable cause) {
        super(cause.getMessage(), cause);
        this.status = -1;
    }

    public SmtpException(int status, String message) {
        super(message);
        this.status = status;
    }
}
