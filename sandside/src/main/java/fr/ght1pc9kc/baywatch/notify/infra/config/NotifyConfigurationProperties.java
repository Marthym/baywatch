package fr.ght1pc9kc.baywatch.notify.infra.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "baywatch.notify")
public record NotifyConfigurationProperties(
        MailerConfigurationProperties mailer
) {
    public record MailerConfigurationProperties(
            String whitelistIps
    ) {
    }
}
