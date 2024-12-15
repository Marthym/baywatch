package fr.ght1pc9kc.baywatch.scraper.infra.adapters.handlers;

import fr.ght1pc9kc.baywatch.scraper.api.ScrapingEventHandler;
import fr.ght1pc9kc.baywatch.scraper.domain.actions.ScrapingLoggerHandler;
import lombok.experimental.Delegate;
import org.springframework.stereotype.Component;

@Component
public class ScrapingLoggerHandlerAdapter implements ScrapingEventHandler {
    @Delegate
    private final ScrapingEventHandler delegate;

    public ScrapingLoggerHandlerAdapter() {
        this.delegate = new ScrapingLoggerHandler();
    }
}
