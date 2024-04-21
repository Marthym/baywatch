package fr.ght1pc9kc.baywatch.scraper.domain.actions;

import fr.ght1pc9kc.baywatch.common.domain.Hasher;
import fr.ght1pc9kc.baywatch.scraper.api.ScrapingErrorsService;
import fr.ght1pc9kc.baywatch.scraper.api.ScrapingEventHandler;
import fr.ght1pc9kc.baywatch.scraper.api.model.AtomFeed;
import fr.ght1pc9kc.baywatch.scraper.api.model.ScrapResult;
import fr.ght1pc9kc.baywatch.scraper.api.model.ScrapingError;
import fr.ght1pc9kc.baywatch.scraper.api.model.ScrapingEventType;
import fr.ght1pc9kc.baywatch.scraper.domain.model.ex.FeedScrapingException;
import fr.ght1pc9kc.entity.api.Entity;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.jetbrains.annotations.VisibleForTesting;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Clock;
import java.time.Instant;
import java.util.EnumSet;

@RequiredArgsConstructor
public class PersistErrorsHandler implements ScrapingEventHandler {
    private final ScrapingErrorsService scrapingErrorsService;

    @Setter(value = AccessLevel.PACKAGE, onMethod = @__({@VisibleForTesting}))
    private Clock clock = Clock.systemUTC();

    @Override
    public Mono<Void> after(ScrapResult result) {
        return Flux.fromIterable(result.errors())
                .filter(err -> {
                    if (err instanceof FeedScrapingException fse) {
                        return fse.getEntity().link() != null;
                    } else {
                        return false;
                    }
                })
                .map(err -> {
                    FeedScrapingException fse = (FeedScrapingException) err;
                    AtomFeed feed = fse.getEntity();
                    assert feed.link() != null : "Feed link must not be null !";

                    Instant now = clock.instant();
                    return Entity.identify(new ScrapingError(now, now, deepFindStatus(fse), fse.getMessage()))
                            .withId(Hasher.identify(feed.link()));
                }).buffer(100)
                .flatMap(scrapingErrorsService::persist)
                .map(Entity::id)
                .collectList()
                .flatMap(scrapingErrorsService::purge);
    }

    private int deepFindStatus(Exception ex) {
        Throwable current = ex;
        while (current != null && !current.getCause().getClass().isAssignableFrom(IllegalArgumentException.class)) {
            current = current.getCause();
        }

        if (current == null) {
            return 0;
        }

        return 42;
    }

    @Override
    public EnumSet<ScrapingEventType> eventTypes() {
        return EnumSet.of(ScrapingEventType.FEED_SCRAPING);
    }
}
