package fr.ght1pc9kc.baywatch.notify.infra.adapters;

import fr.ght1pc9kc.baywatch.admin.api.AppConfigurationService;
import fr.ght1pc9kc.baywatch.notify.domain.model.SmtpServerConfig;
import fr.ght1pc9kc.baywatch.notify.domain.ports.SmtpConfigurationPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Component
@RequiredArgsConstructor
public class SmtpConfigurationAdapter implements SmtpConfigurationPort {
    private static final String MAIL_SMTP_PREFIX = "mail.smtp.";
    private static final Duration MAIL_QUEUE_POLLING_INTERVAL = Duration.ofSeconds(120);
    private final AppConfigurationService appConfigurationService;

    @Override
    public Mono<SmtpServerConfig> get() {
        return appConfigurationService.get(MAIL_SMTP_PREFIX)
                .map(params -> new SmtpServerConfig(
                        params.getValue(MAIL_SMTP_PREFIX + "host"),
                        Integer.parseInt(params.getValue(MAIL_SMTP_PREFIX + "port")),
                        params.getValue(MAIL_SMTP_PREFIX + "from"),
                        Boolean.parseBoolean(params.getValue(MAIL_SMTP_PREFIX + "secure")),
                        params.getValue(MAIL_SMTP_PREFIX + "username"),
                        params.getValue(MAIL_SMTP_PREFIX + "password"),
                        params.getValue(MAIL_SMTP_PREFIX + "ssl.protocols"),
                        Boolean.parseBoolean(params.getValue(MAIL_SMTP_PREFIX + "ssl.checkserveridentity")),
                        Boolean.parseBoolean(params.getValue(MAIL_SMTP_PREFIX + "requireTls"))
                ));
    }

    @Override
    public Duration getMailQueuePollingInterval() {
        return MAIL_QUEUE_POLLING_INTERVAL;
    }
}
