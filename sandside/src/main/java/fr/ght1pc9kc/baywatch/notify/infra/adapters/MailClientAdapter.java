package fr.ght1pc9kc.baywatch.notify.infra.adapters;

import fr.ght1pc9kc.baywatch.notify.api.MailClient;
import fr.ght1pc9kc.baywatch.notify.domain.services.MailClientImpl;
import fr.ght1pc9kc.baywatch.notify.domain.ports.SmtpPort;
import lombok.experimental.Delegate;
import org.springframework.stereotype.Component;

@Component
public class MailClientAdapter implements MailClient {
    @Delegate
    private final MailClient delegate;

    public MailClientAdapter(SmtpPort smtp) {
        this.delegate = new MailClientImpl(smtp);
    }
}
