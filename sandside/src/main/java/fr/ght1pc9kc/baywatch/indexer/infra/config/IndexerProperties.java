package fr.ght1pc9kc.baywatch.indexer.infra.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;

@ConstructorBinding
@ConfigurationProperties(prefix = "baywatch.indexer")
public record IndexerProperties(
        boolean enable,
        String directory
) {
}
