package fr.ght1pc9kc.baywatch.scraper.api;

import fr.ght1pc9kc.baywatch.scraper.api.model.ScrapingError;
import fr.ght1pc9kc.entity.api.Entity;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collection;
import java.util.logging.Level;

public interface ScrapingErrorsService {
    Flux<Entity<ScrapingError>> persist(Collection<Entity<ScrapingError>> errors);

    Flux<Entity<ScrapingError>> list(Collection<String> feedsIds);

    Mono<Void> purge(Collection<String> notInFeedsIds);

    Level level(ScrapingError error);

    String filterMessage(ScrapingError error);
}
