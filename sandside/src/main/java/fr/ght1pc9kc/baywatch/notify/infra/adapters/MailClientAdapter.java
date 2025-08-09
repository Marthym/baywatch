package fr.ght1pc9kc.baywatch.notify.infra.adapters;

import fr.ght1pc9kc.baywatch.notify.api.MailClient;
import fr.ght1pc9kc.baywatch.notify.domain.MailTemplateService;
import fr.ght1pc9kc.baywatch.notify.domain.ports.MailQueuePersistencePort;
import fr.ght1pc9kc.baywatch.notify.domain.ports.NotifyAuthenticationPort;
import fr.ght1pc9kc.baywatch.notify.domain.ports.NotifyClientInfoPort;
import fr.ght1pc9kc.baywatch.notify.domain.services.MailClientImpl;
import lombok.experimental.Delegate;
import org.springframework.stereotype.Component;

@Component
public class MailClientAdapter implements MailClient {
    @Delegate
    private final MailClient delegate;

    public MailClientAdapter(
            MailTemplateService mailTemplateService, NotifyAuthenticationPort notifyAuthenticationPort,
            NotifyClientInfoPort localeFacadePort, MailQueuePersistencePort mailQueuePersistencePort) {
        this.delegate = new MailClientImpl(
                mailTemplateService, notifyAuthenticationPort, localeFacadePort, mailQueuePersistencePort);
    }
}
