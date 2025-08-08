package fr.ght1pc9kc.baywatch.notify.infra.adapters;

import fr.ght1pc9kc.baywatch.admin.api.AppConfigurationService;
import fr.ght1pc9kc.baywatch.notify.domain.model.SmtpServerConfig;
import fr.ght1pc9kc.baywatch.notify.domain.ports.SmtpConfigurationPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class SmtpConfigurationAdapter implements SmtpConfigurationPort {
    private static final String MAIL_SMTP_PREFIX = "mail.smtp.";
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
                        params.getValue(MAIL_SMTP_PREFIX + "cipher"),
                        Boolean.parseBoolean(params.getValue(MAIL_SMTP_PREFIX + "requireTls"))
                ));
    }
}
