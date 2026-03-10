package fr.ght1pc9kc.baywatch.security.infra.adapters.subscribers;

import fr.ght1pc9kc.baywatch.security.domain.ports.KeyValuePersistencePort;
import fr.ght1pc9kc.baywatch.security.domain.ports.MailSenderPort;
import fr.ght1pc9kc.baywatch.security.domain.ports.UserEventPublisherPort;
import fr.ght1pc9kc.baywatch.security.domain.subscribers.WelcomeMailUserSubscriber;
import jakarta.annotation.PreDestroy;
import lombok.experimental.Delegate;
import org.springframework.stereotype.Component;

@Component
public class WelcomeMailUserSubscriberAdapter {
    @Delegate
    private final WelcomeMailUserSubscriber delegate;

    public WelcomeMailUserSubscriberAdapter(
            UserEventPublisherPort publisher, MailSenderPort mailSenderPort, KeyValuePersistencePort keyValuePersistencePort) {
        this.delegate = new WelcomeMailUserSubscriber(publisher, mailSenderPort, keyValuePersistencePort);
    }

    @PreDestroy
    public void destroy() {
        this.delegate.destroy();
    }
}
