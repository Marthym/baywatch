package fr.ght1pc9kc.baywatch.scraper.domain.actions;

import fr.ght1pc9kc.baywatch.scraper.api.ScrapingHandler;
import fr.ght1pc9kc.baywatch.scraper.infra.config.ScraperProperties;
import fr.ght1pc9kc.baywatch.techwatch.api.model.News;
import fr.ght1pc9kc.baywatch.techwatch.domain.model.QueryContext;
import fr.ght1pc9kc.baywatch.techwatch.domain.ports.NewsPersistencePort;
import fr.ght1pc9kc.baywatch.techwatch.domain.ports.StatePersistencePort;
import fr.ght1pc9kc.juery.api.Criteria;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;

import static fr.ght1pc9kc.baywatch.common.api.model.EntitiesProperties.NEWS_ID;
import static fr.ght1pc9kc.baywatch.common.api.model.EntitiesProperties.PUBLICATION;
import static fr.ght1pc9kc.baywatch.common.api.model.EntitiesProperties.SHARED;

@Slf4j
@RequiredArgsConstructor
public class PurgeNewsHandler implements ScrapingHandler {
    private static final int DELETE_BUFFER_SIZE = 500;
    private final NewsPersistencePort newsPersistence;
    private final StatePersistencePort statePersistence;
    private final ScraperProperties scraperProperties;

    @Setter
    @Accessors(fluent = true)
    private Clock clock = Clock.systemUTC();

    @Override
    public Mono<Void> before() {
        LocalDateTime maxPublicationPasDate = LocalDateTime.now(clock).minus(scraperProperties.conservation());
        Criteria criteria = Criteria.property(PUBLICATION).lt(maxPublicationPasDate);
        return newsPersistence.list(QueryContext.all(criteria))
                .map(News::getId)
                .collectList()
                .flatMapMany(this::keepStaredNewsIds)
                .buffer(DELETE_BUFFER_SIZE)
                .flatMap(newsPersistence::delete)
                .onErrorContinue((t, o) -> {
                    log.error("{}: {}", t.getCause(), t.getLocalizedMessage());
                    log.debug("STACKTRACE", t);
                }).then();
    }

    private Flux<String> keepStaredNewsIds(Collection<String> newsIds) {
        Criteria isStaredCriteria = Criteria.property(NEWS_ID).in(newsIds)
                .and(Criteria.property(SHARED).eq(true));
        return statePersistence.list(QueryContext.all(isStaredCriteria))
                .map(e -> e.id)
                .collectList()
                .flatMapMany(stareds -> {
                    Collection<String> toBeDeleted = new ArrayList<>(newsIds);
                    toBeDeleted.removeAll(stareds);
                    return Flux.fromIterable(toBeDeleted);
                });
    }

}
