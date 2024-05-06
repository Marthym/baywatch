package fr.ght1pc9kc.baywatch.scraper.infra.adapters.handlers;

import fr.ght1pc9kc.baywatch.admin.api.model.CounterProvider;
import fr.ght1pc9kc.baywatch.scraper.api.ScrapingErrorsService;
import fr.ght1pc9kc.baywatch.scraper.api.ScrapingEventHandler;
import fr.ght1pc9kc.baywatch.scraper.domain.actions.PersistErrorsHandler;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.experimental.Delegate;
import org.springframework.stereotype.Component;

@Component
public class PersistErrorsHandlerAdapter implements CounterProvider, ScrapingEventHandler {
    @Delegate
    private final PersistErrorsHandler persistErrorsHandler;

    public PersistErrorsHandlerAdapter(MeterRegistry registry, ScrapingErrorsService scrapingErrorsService) {
        persistErrorsHandler = new PersistErrorsHandler(scrapingErrorsService);

        Gauge.builder("bw.scraping.errors", persistErrorsHandler::getLastErrorCount)
                .description("Feed Scraping Duration")
                .register(registry);
    }
}
