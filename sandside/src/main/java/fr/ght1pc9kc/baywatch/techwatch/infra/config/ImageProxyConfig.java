package fr.ght1pc9kc.baywatch.techwatch.infra.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;

@ConstructorBinding
@ConfigurationProperties(prefix = "baywatch.imgproxy")
public record ImageProxyConfig(
        String signingKey,
        String signingSalt,
        String pathBase,
        String processing,
        String extension
) {
}
