package fr.ght1pc9kc.baywatch.scrapper.infra.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;

import java.time.Duration;
import java.time.Period;
import java.util.Set;

@ConstructorBinding
@ConfigurationProperties(prefix = "baywatch.scrapper")
public record ScrapperProperties(
        boolean start,
        Duration frequency,
        Duration timeout,
        Period conservation,
        Set<String> supportedScheme) {
}
