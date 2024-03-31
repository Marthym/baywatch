package fr.ght1pc9kc.baywatch.scraper.domain;

import fr.ght1pc9kc.baywatch.common.domain.QueryContext;
import fr.ght1pc9kc.baywatch.scraper.api.ScrapingErrorsService;
import fr.ght1pc9kc.baywatch.scraper.api.model.ScrapingError;
import fr.ght1pc9kc.baywatch.scraper.domain.ports.ScrapingAuthentFacade;
import fr.ght1pc9kc.baywatch.scraper.domain.ports.ScrapingErrorPersistencePort;
import fr.ght1pc9kc.entity.api.Entity;
import fr.ght1pc9kc.juery.api.Criteria;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collection;

import static fr.ght1pc9kc.baywatch.common.api.model.EntitiesProperties.ID;

@RequiredArgsConstructor
public class ScrapingErrorsServiceImpl implements ScrapingErrorsService {
    private final ScrapingErrorPersistencePort persistencePort;
    private final ScrapingAuthentFacade authentFacade;

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
        return persistencePort.delete(query);
    }
}
