package fr.ght1pc9kc.baywatch.notify.domain.ports;

import fr.ght1pc9kc.baywatch.notify.domain.model.SmtpServerConfig;
import reactor.core.publisher.Mono;

import java.time.Duration;

public interface SmtpConfigurationPort {
    Mono<SmtpServerConfig> get();
    Duration getMailQueuePollingInterval();
}
