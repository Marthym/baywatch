package fr.ght1pc9kc.baywatch.scraper.domain.actions;

import fr.ght1pc9kc.baywatch.admin.api.model.Counter;
import fr.ght1pc9kc.baywatch.admin.api.model.CounterGroup;
import fr.ght1pc9kc.baywatch.admin.api.model.CounterProvider;
import fr.ght1pc9kc.baywatch.common.api.model.HeroIcons;
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

import javax.net.ssl.SSLHandshakeException;
import java.time.Clock;
import java.time.Instant;
import java.util.EnumSet;
import java.util.concurrent.atomic.AtomicInteger;

import static java.util.Objects.isNull;

@RequiredArgsConstructor
public class PersistErrorsHandler implements ScrapingEventHandler, CounterProvider {
    private final AtomicInteger lastErrorCount = new AtomicInteger(0);
    private final ScrapingErrorsService scrapingErrorsService;

    @Setter(value = AccessLevel.PACKAGE, onMethod = @__({@VisibleForTesting}))
    private Clock clock = Clock.systemUTC();

    @Override
    public Mono<Void> after(ScrapResult result) {
        lastErrorCount.set(result.errors().size());
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
                    return Entity.identify(new ScrapingError(deepFindStatus(fse), now, now, fse.getMessage()))
                            .withId(Hasher.identify(feed.link()));
                }).buffer(100)
                .flatMap(scrapingErrorsService::persist)
                .map(Entity::id)
                .collectList()
                .flatMap(scrapingErrorsService::purge);
    }

    private int deepFindStatus(Exception ex) {
        try {
            Throwable current = ex;

            while (true) {
                if (isNull(current)) {
                    return 0;

                } else if (SSLHandshakeException.class.isAssignableFrom(current.getClass()) ||
                        "SearchDomainUnknownHostException".equals(current.getClass().getSimpleName())) {
                    return 500;

                } else if ("ConnectTimeoutException".equals(current.getClass().getSimpleName()) ||
                        "ReadTimeoutException".equals(current.getClass().getSimpleName())) {
                    return 408;

                } else if ("DecodingException".equals(current.getClass().getSimpleName())) {
                    return 599;

                } else if (IllegalArgumentException.class.isAssignableFrom(current.getClass()) ||
                        IllegalStateException.class.isAssignableFrom(current.getClass())) {
                    String extractedNumber = current.getLocalizedMessage().replaceAll("\\D", "");
                    int status = (!extractedNumber.isEmpty()) ? Integer.parseInt(extractedNumber) : 200;
                    return Math.clamp(status, 200, 599);

                } else {
                    current = current.getCause();
                }
            }

        } catch (Exception ignore) {
            return 418;
        }
    }

    @Override
    public EnumSet<ScrapingEventType> eventTypes() {
        return EnumSet.of(ScrapingEventType.FEED_SCRAPING);
    }

    @Override
    public CounterGroup group() {
        return CounterGroup.SCRAPER;
    }

    @Override
    public Mono<Counter> computeCounter() {
        return Mono.fromCallable(() -> Counter.create(
                "Feeds Errors",
                HeroIcons.EXCLAMATION_TRIANGLE_ICON,
                Integer.toString(lastErrorCount.intValue()),
                "error(s) during last scraping"));
    }

    public int getLastErrorCount() {
        return lastErrorCount.get();
    }
}
