package fr.ght1pc9kc.baywatch.notify.domain.exceptions;

import fr.ght1pc9kc.baywatch.common.api.exceptions.TranslatableException;

public class MailLimitExceededException extends NotifyModuleException implements TranslatableException {
    public MailLimitExceededException(String message) {
        super(message);
    }

    @Override
    public String getTranslationKey() {
        return "error.mail.limit.exceeded";
    }

    @Override
    public String classification() {
        return "UNAUTHORIZED";
    }
}
