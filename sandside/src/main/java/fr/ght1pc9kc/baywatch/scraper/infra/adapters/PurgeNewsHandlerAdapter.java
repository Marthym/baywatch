package fr.ght1pc9kc.baywatch.scraper.infra.adapters;

import fr.ght1pc9kc.baywatch.scraper.api.ScrapingEventHandler;
import fr.ght1pc9kc.baywatch.scraper.domain.actions.PurgeNewsHandler;
import fr.ght1pc9kc.baywatch.scraper.infra.config.ScraperApplicationProperties;
import fr.ght1pc9kc.baywatch.techwatch.domain.ports.NewsPersistencePort;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Delegate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PurgeNewsHandlerAdapter implements ScrapingEventHandler {
    @Delegate
    private final PurgeNewsHandler delegate;

    @Autowired
    public PurgeNewsHandlerAdapter(NewsPersistencePort newsPersistencePort, ScraperApplicationProperties props) {
        this.delegate = new PurgeNewsHandler(newsPersistencePort, props);
    }
}
