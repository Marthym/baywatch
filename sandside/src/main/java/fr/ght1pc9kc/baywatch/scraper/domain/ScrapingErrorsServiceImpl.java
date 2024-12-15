package fr.ght1pc9kc.baywatch.scraper.domain;

import fr.ght1pc9kc.baywatch.common.domain.QueryContext;
import fr.ght1pc9kc.baywatch.scraper.api.ScrapingErrorsService;
import fr.ght1pc9kc.baywatch.scraper.api.model.ScrapingError;
import fr.ght1pc9kc.baywatch.scraper.domain.ports.ScrapingAuthentFacade;
import fr.ght1pc9kc.baywatch.scraper.domain.ports.ScrapingErrorPersistencePort;
import fr.ght1pc9kc.entity.api.Entity;
import fr.ght1pc9kc.juery.api.Criteria;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Clock;
import java.time.Duration;
import java.util.Collection;
import java.util.logging.Level;

import static fr.ght1pc9kc.baywatch.common.api.model.EntitiesProperties.ID;

@RequiredArgsConstructor
public class ScrapingErrorsServiceImpl implements ScrapingErrorsService {
    private final ScrapingErrorPersistencePort persistencePort;
    private final ScrapingAuthentFacade authentFacade;

    @Setter
    private Clock clock = Clock.systemUTC();

    @Override
    public Flux<Entity<ScrapingError>> persist(Collection<Entity<ScrapingError>> errors) {
        return authentFacade.getConnectedUser()
                .filter(authentFacade::hasSystemRole)
                .switchIfEmpty(Mono.error(() -> new IllegalAccessException("Persis scraping error not permitted !")))
                .flatMapMany(u -> persistencePort.persist(errors));
    }

    @Override
    public Flux<Entity<ScrapingError>> list(Collection<String> feedsIds) {
        QueryContext query = QueryContext.all(Criteria.property(ID).in(feedsIds));
        return persistencePort.list(query);
    }

    @Override
    public Mono<Void> purge(Collection<String> notInFeedsIds) {
        QueryContext query = QueryContext.all(Criteria.not(Criteria.property(ID).in(notInFeedsIds)));
        return authentFacade.getConnectedUser()
                .filter(authentFacade::hasSystemRole)
                .switchIfEmpty(Mono.error(() -> new IllegalAccessException("Persis scraping error not permitted !")))
                .flatMap(u -> persistencePort.delete(query));
    }

    @Override
    public Level level(ScrapingError error) {
        if (error.code() > 499 || error.since().isBefore(clock.instant().minus(Duration.ofDays(90)))) {
            return Level.SEVERE;
        } else {
            return Level.WARNING;
        }
    }
}
