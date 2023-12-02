package fr.ght1pc9kc.baywatch.scraper.domain.actions;

import fr.ght1pc9kc.baywatch.admin.api.model.Counter;
import fr.ght1pc9kc.baywatch.admin.api.model.CounterGroup;
import fr.ght1pc9kc.baywatch.admin.api.model.CounterProvider;
import fr.ght1pc9kc.baywatch.common.api.ScrapingEventHandler;
import fr.ght1pc9kc.baywatch.common.api.model.HeroIcons;
import fr.ght1pc9kc.baywatch.scraper.api.model.ScrapResult;
import lombok.AccessLevel;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.VisibleForTesting;
import reactor.core.publisher.Mono;

import java.time.Clock;
import java.time.Duration;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
public class ScrapingDurationCounter implements CounterProvider, ScrapingEventHandler {
    private final AtomicReference<Long> startNanoTime = new AtomicReference<>(null);
    private final AtomicReference<Duration> lastDuration = new AtomicReference<>(Duration.ZERO);
    private final AtomicReference<String> lastInstant = new AtomicReference<>("scraping in progress...");

    @VisibleForTesting
    @Setter(AccessLevel.PACKAGE)
    private Clock clock = Clock.systemUTC();

    @Override
    public Mono<Void> before() {
        startNanoTime.set(clock.millis());
        return ScrapingEventHandler.super.before();
    }

    @Override
    public Mono<Void> after(ScrapResult result) {
        return Mono.fromCallable(() -> {
            if (log.isDebugEnabled()) {
                String duration = getCurrentDuration(
                        startNanoTime.get() == null ? Duration.ZERO : Duration.ofMillis(clock.millis() - startNanoTime.get())
                );
                log.debug("Scraping duration: {}", duration);
            }
            return result;
        }).then();
    }

    @Override
    public void onTerminate() {
        lastInstant.set(clock.instant().toString());
        lastDuration.set(
                startNanoTime.get() == null ? Duration.ZERO : Duration.ofMillis(clock.millis() - startNanoTime.get())
        );
        startNanoTime.set(null);
        ScrapingEventHandler.super.onTerminate();
    }

    private static String getCurrentDuration(Duration lastDuration) {
        return String.format("%02ds %02dms", lastDuration.toSecondsPart(), lastDuration.toMillisPart());
    }

    @Override
    public CounterGroup group() {
        return CounterGroup.TECHWATCH;
    }

    @Override
    public Mono<Counter> computeCounter() {
        return Mono.just(Counter.create(
                "Scraping Duration",
                HeroIcons.CLOUD_ARROWUP_ICON,
                getCurrentDuration(lastDuration.get()),
                lastInstant.get()));
    }

    public Duration getLastDuration() {
        return lastDuration.get();
    }

    @Override
    public Set<String> eventTypes() {
        return Set.of("FEED_SCRAPING");
    }
}
