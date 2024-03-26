package fr.ght1pc9kc.baywatch.scraper.domain.actions;

import fr.ght1pc9kc.baywatch.scraper.api.ScrapingEventHandler;
import fr.ght1pc9kc.baywatch.scraper.api.model.ScrapingEventType;
import fr.ght1pc9kc.baywatch.scraper.infra.config.ScraperApplicationProperties;
import fr.ght1pc9kc.baywatch.techwatch.domain.model.QueryContext;
import fr.ght1pc9kc.baywatch.techwatch.domain.ports.NewsPersistencePort;
import fr.ght1pc9kc.juery.api.Criteria;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.EnumSet;

import static fr.ght1pc9kc.baywatch.common.api.model.EntitiesProperties.PUBLICATION;

@Slf4j
@RequiredArgsConstructor
public class PurgeNewsHandler implements ScrapingEventHandler {
    private final NewsPersistencePort newsPersistence;
    private final ScraperApplicationProperties scraperProperties;

    @Setter
    @Accessors(fluent = true)
    private Clock clock = Clock.systemUTC();

    @Override
    public Mono<Void> before() {
        LocalDateTime maxPublicationPasDate = LocalDateTime.now(clock).minus(scraperProperties.conservation());
        Criteria criteria = Criteria.property(PUBLICATION).lt(maxPublicationPasDate);
        return newsPersistence.listId(QueryContext.all(criteria)).collectList()
                .flatMap(newsPersistence::delete)
                .onErrorContinue((t, o) -> {
                    log.error("{}: {}", t.getCause(), t.getLocalizedMessage());
                    log.debug("STACKTRACE", t);
                })
                .then()
                .doOnTerminate(() -> log.debug("PurgeNewsHandler terminated."));
    }

    @Override
    public EnumSet<ScrapingEventType> eventTypes() {
        return EnumSet.of(ScrapingEventType.FEED_SCRAPING);
    }
}
