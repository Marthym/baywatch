package fr.ght1pc9kc.baywatch.scrapper.domain.actions;

import fr.ght1pc9kc.baywatch.techwatch.api.model.News;
import fr.ght1pc9kc.baywatch.scrapper.api.ScrappingHandler;
import fr.ght1pc9kc.baywatch.techwatch.domain.model.QueryContext;
import fr.ght1pc9kc.baywatch.techwatch.domain.ports.NewsPersistencePort;
import fr.ght1pc9kc.baywatch.scrapper.infra.config.ScrapperProperties;
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
import java.util.Map;

import static fr.ght1pc9kc.baywatch.common.api.model.EntitiesProperties.*;

@Slf4j
@RequiredArgsConstructor
public class PurgeNewsHandler implements ScrappingHandler {
    private static final int DELETE_BUFFER_SIZE = 500;
    private final NewsPersistencePort newsPersistence;
    private final ScrapperProperties scrapperProperties;

    @Setter
    @Accessors(fluent = true)
    private Clock clock = Clock.systemUTC();

    @Override
    public Mono<Void> before() {
        LocalDateTime maxPublicationPasDate = LocalDateTime.now(clock).minus(scrapperProperties.conservation);
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
        return newsPersistence.listState(isStaredCriteria)
                .map(Map.Entry::getKey)
                .collectList()
                .flatMapMany(stareds -> {
                    Collection<String> toBeDeleted = new ArrayList<>(newsIds);
                    toBeDeleted.removeAll(stareds);
                    return Flux.fromIterable(toBeDeleted);
                });
    }

}
