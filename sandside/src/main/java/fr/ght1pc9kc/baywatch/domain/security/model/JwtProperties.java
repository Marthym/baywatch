package fr.ght1pc9kc.baywatch.domain.security.model;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@Value
@Getter(AccessLevel.NONE)
@ConfigurationProperties(prefix = "security")
public class JwtProperties {
    public String secretKey = "flzxsqcysyhljt";
    public Duration validity = Duration.ofHours(1);
}

