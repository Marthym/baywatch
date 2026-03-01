package fr.ght1pc9kc.baywatch.security.infra.adapters.subscribers;

import fr.ght1pc9kc.baywatch.security.domain.ports.ReactiveContextPort;
import fr.ght1pc9kc.baywatch.security.domain.ports.MailSenderPort;
import fr.ght1pc9kc.baywatch.security.domain.ports.UserEventPublisherPort;
import fr.ght1pc9kc.baywatch.security.domain.subscribers.WelcomeMailUserSubscriber;
import lombok.experimental.Delegate;
import org.springframework.stereotype.Component;

@Component
public class WelcomeMailUserSubscriberAdapter {
    @Delegate
    private final WelcomeMailUserSubscriber delegate;

    public WelcomeMailUserSubscriberAdapter(
            UserEventPublisherPort publisher, MailSenderPort mailSenderPort, ReactiveContextPort clientInfoPort) {
        this.delegate = new WelcomeMailUserSubscriber(publisher, mailSenderPort, clientInfoPort);
    }
}
