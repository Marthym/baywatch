package fr.ght1pc9kc.baywatch.scraper.domain.ports;

import fr.ght1pc9kc.baywatch.common.domain.QueryContext;
import fr.ght1pc9kc.baywatch.scraper.api.model.ScrapingError;
import fr.ght1pc9kc.entity.api.Entity;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collection;

public interface ScrapingErrorPersistencePort {
    Flux<Entity<ScrapingError>> persist(Collection<Entity<ScrapingError>> errors);

    Flux<Entity<ScrapingError>> list(QueryContext query);

    Mono<Void> delete(QueryContext query);
}
