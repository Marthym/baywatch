package fr.ght1pc9kc.baywatch.infra.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;

import java.time.Duration;
import java.time.Period;

@ConstructorBinding
@RequiredArgsConstructor
@ConfigurationProperties(prefix = "baywatch.scrapper")
public class ScrapperProperties {
    public final boolean start;
    public final Duration frequency;
    public final Period conservation;
}
