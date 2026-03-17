package fr.ght1pc9kc.baywatch.notify.infra.adapters;

import fr.ght1pc9kc.baywatch.notify.api.MailClient;
import fr.ght1pc9kc.baywatch.notify.domain.MailTemplateService;
import fr.ght1pc9kc.baywatch.notify.domain.ports.MailQueuePersistencePort;
import fr.ght1pc9kc.baywatch.notify.domain.ports.NotifyAuthenticationPort;
import fr.ght1pc9kc.baywatch.notify.domain.ports.NotifyClientInfoPort;
import fr.ght1pc9kc.baywatch.notify.domain.services.MailClientImpl;
import fr.ght1pc9kc.baywatch.notify.infra.config.NotifyConfigurationProperties;
import lombok.experimental.Delegate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
public class MailClientAdapter implements MailClient {
    @Delegate
    private final MailClient delegate;

    public MailClientAdapter(
            MailTemplateService mailTemplateService, NotifyAuthenticationPort notifyAuthenticationPort,
            NotifyClientInfoPort localeFacadePort, MailQueuePersistencePort mailQueuePersistencePort,
            NotifyConfigurationProperties configurationProperties, @Value("${spring.application.name}") String applicationName) {
        Set<String> whitelistedIps = Stream.of(configurationProperties.mailer().whitelistIps().split(","))
                .collect(Collectors.toUnmodifiableSet());
        this.delegate = new MailClientImpl(
                mailTemplateService, notifyAuthenticationPort, localeFacadePort, mailQueuePersistencePort,
                whitelistedIps, applicationName);
    }
}
