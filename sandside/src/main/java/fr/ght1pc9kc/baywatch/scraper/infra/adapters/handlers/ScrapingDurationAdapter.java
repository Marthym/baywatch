package fr.ght1pc9kc.baywatch.scraper.infra.adapters.handlers;

import fr.ght1pc9kc.baywatch.admin.api.model.CounterProvider;
import fr.ght1pc9kc.baywatch.scraper.api.ScrapingEventHandler;
import fr.ght1pc9kc.baywatch.scraper.domain.actions.ScrapingDurationCounter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.TimeGauge;
import lombok.experimental.Delegate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class ScrapingDurationAdapter implements CounterProvider, ScrapingEventHandler {
    @Delegate
    private final ScrapingDurationCounter delegate;

    public ScrapingDurationAdapter(MeterRegistry registry) {
        this.delegate = new ScrapingDurationCounter();

        TimeGauge.builder("bw.scraping", () -> delegate.getLastDuration().toMillis(), TimeUnit.MILLISECONDS)
                .description("Feed Scraping Duration")
                .register(registry);
    }
}
