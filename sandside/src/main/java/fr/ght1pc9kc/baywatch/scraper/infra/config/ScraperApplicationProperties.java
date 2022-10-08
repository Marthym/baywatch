package fr.ght1pc9kc.baywatch.scraper.infra.config;

import fr.ght1pc9kc.baywatch.scraper.domain.model.ScraperProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;

import java.time.Duration;
import java.time.Period;

@ConstructorBinding
@ConfigurationProperties(prefix = "baywatch.scraper")
public record ScraperApplicationProperties(
        boolean enable,
        Duration frequency,
        Period conservation,
        Duration timeout,
        DnsProperties dns) implements ScraperProperties {

    @ConstructorBinding
    public record DnsProperties(
            Duration timeout
    ) {
    }
}
