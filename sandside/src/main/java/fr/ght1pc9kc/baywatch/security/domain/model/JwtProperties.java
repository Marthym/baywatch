package fr.ght1pc9kc.baywatch.security.domain.model;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "security")
public record JwtProperties(
        String secretKey,
        Duration validity
) {
}
