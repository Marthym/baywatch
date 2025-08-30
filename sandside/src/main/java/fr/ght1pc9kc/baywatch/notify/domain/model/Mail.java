package fr.ght1pc9kc.baywatch.notify.domain.model;

import lombok.Builder;
import lombok.Singular;

import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;

import static java.util.Objects.isNull;

/**
 * The mail entity
 *
 * @param to          The target of the message
 * @param subject     The subject of the message
 * @param message     The message body
 * @param attachments The possible attachments
 */
@Builder(toBuilder = true)
public record Mail(String to,
                   Collection<String> bcc,
                   String subject,
                   String message,
                   @Singular Collection<Path> attachments) {
    public Mail {
        if (isNull(bcc)) {
            bcc = Collections.emptyList();
        }
    }
}
