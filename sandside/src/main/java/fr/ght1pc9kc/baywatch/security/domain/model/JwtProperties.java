package fr.ght1pc9kc.baywatch.security.domain.model;

import lombok.AccessLevel;
import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@Getter(AccessLevel.NONE)
@ConfigurationProperties(prefix = "security")
public record JwtProperties(
        String secretKey,
        Duration validity
) {
}
