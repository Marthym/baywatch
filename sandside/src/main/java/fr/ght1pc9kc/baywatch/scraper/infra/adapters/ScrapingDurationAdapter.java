package fr.ght1pc9kc.baywatch.scraper.infra.adapters;

import fr.ght1pc9kc.baywatch.admin.api.model.CounterProvider;
import fr.ght1pc9kc.baywatch.common.api.EventHandler;
import fr.ght1pc9kc.baywatch.scraper.domain.actions.ScrapingDurationCounter;
import lombok.experimental.Delegate;
import org.springframework.stereotype.Component;

@Component
public class ScrapingDurationAdapter implements CounterProvider, EventHandler {
    @Delegate
    private final ScrapingDurationCounter delegate;

    public ScrapingDurationAdapter() {
        this.delegate = new ScrapingDurationCounter();
    }
}
