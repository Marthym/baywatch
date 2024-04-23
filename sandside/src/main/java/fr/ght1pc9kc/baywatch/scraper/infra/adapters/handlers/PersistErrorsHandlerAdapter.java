package fr.ght1pc9kc.baywatch.scraper.infra.adapters.handlers;

import fr.ght1pc9kc.baywatch.scraper.api.ScrapingErrorsService;
import fr.ght1pc9kc.baywatch.scraper.api.ScrapingEventHandler;
import fr.ght1pc9kc.baywatch.scraper.domain.actions.PersistErrorsHandler;
import lombok.experimental.Delegate;
import org.springframework.stereotype.Component;

@Component
public class PersistErrorsHandlerAdapter implements ScrapingEventHandler {
    @Delegate
    private final PersistErrorsHandler persistErrorsHandler;

    public PersistErrorsHandlerAdapter(ScrapingErrorsService scrapingErrorsService) {
        persistErrorsHandler = new PersistErrorsHandler(scrapingErrorsService);
    }
}
