package fr.ght1pc9kc.baywatch.scrapper.infra.adapters;

import fr.ght1pc9kc.baywatch.scrapper.api.ScrappingHandler;
import fr.ght1pc9kc.baywatch.scrapper.infra.config.ScrapperProperties;
import fr.ght1pc9kc.baywatch.techwatch.domain.ports.NewsPersistencePort;
import fr.ght1pc9kc.baywatch.scrapper.domain.actions.PurgeNewsHandler;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Delegate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PurgeNewsHandlerAdapter implements ScrappingHandler {
    @Delegate
    private final PurgeNewsHandler delegate;

    @Autowired
    public PurgeNewsHandlerAdapter(NewsPersistencePort newsPersistencePort, ScrapperProperties props) {
        this.delegate = new PurgeNewsHandler(newsPersistencePort, props);
    }
}
