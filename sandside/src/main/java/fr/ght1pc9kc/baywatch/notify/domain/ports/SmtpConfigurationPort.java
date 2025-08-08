package fr.ght1pc9kc.baywatch.notify.domain.ports;

import fr.ght1pc9kc.baywatch.notify.domain.model.SmtpServerConfig;
import reactor.core.publisher.Mono;

public interface SmtpConfigurationPort {
    Mono<SmtpServerConfig> get();
}
