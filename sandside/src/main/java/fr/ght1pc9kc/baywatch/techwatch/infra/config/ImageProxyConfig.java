package fr.ght1pc9kc.baywatch.techwatch.infra.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;

@ConstructorBinding
@ConfigurationProperties(prefix = "baywatch.imgproxy")
@ConditionalOnProperty(prefix = "baywatch.imgproxy", name = "enable", havingValue = "true")
public record ImageProxyConfig(
        String signingKey,
        String signingSalt,
        String pathBase,
        String extension
) {
}
