package fr.ght1pc9kc.baywatch.scraper.infra.adapters;

import fr.ght1pc9kc.baywatch.common.api.EventHandler;
import fr.ght1pc9kc.baywatch.scraper.domain.actions.PurgeNewsHandler;
import fr.ght1pc9kc.baywatch.scraper.infra.config.ScraperProperties;
import fr.ght1pc9kc.baywatch.techwatch.domain.ports.NewsPersistencePort;
import fr.ght1pc9kc.baywatch.techwatch.domain.ports.StatePersistencePort;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Delegate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PurgeNewsHandlerAdapter implements EventHandler {
    @Delegate
    private final PurgeNewsHandler delegate;

    @Autowired
    public PurgeNewsHandlerAdapter(NewsPersistencePort newsPersistencePort, StatePersistencePort statePersistencePort,
                                   ScraperProperties props) {
        this.delegate = new PurgeNewsHandler(newsPersistencePort, statePersistencePort, props);
    }
}
