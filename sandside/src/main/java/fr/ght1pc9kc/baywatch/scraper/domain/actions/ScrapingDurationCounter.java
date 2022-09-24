package fr.ght1pc9kc.baywatch.scraper.domain.actions;

import fr.ght1pc9kc.baywatch.admin.api.model.Counter;
import fr.ght1pc9kc.baywatch.admin.api.model.CounterGroup;
import fr.ght1pc9kc.baywatch.admin.api.model.CounterProvider;
import fr.ght1pc9kc.baywatch.common.api.EventHandler;
import fr.ght1pc9kc.baywatch.common.api.HeroIcons;
import lombok.AccessLevel;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.VisibleForTesting;
import reactor.core.publisher.Mono;

import java.time.Clock;
import java.time.Duration;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
public class ScrapingDurationCounter implements CounterProvider, EventHandler {
    private final AtomicReference<Long> startNanoTime = new AtomicReference<>(null);
    private final AtomicReference<String> lastDuration = new AtomicReference<>("...");
    private final AtomicReference<String> lastInstant = new AtomicReference<>("scraping in progress...");

    @VisibleForTesting
    @Setter(AccessLevel.PACKAGE)
    private Clock clock = Clock.systemUTC();

    @Override
    public Mono<Void> before() {
        startNanoTime.set(clock.millis());
        return EventHandler.super.before();
    }

    @Override
    public Mono<Void> after(int persisted) {
        if (log.isDebugEnabled()) {
            String duration = getCurrentDuration();
            log.debug("Scraping finished, load {} news in {}", persisted, duration);
        }
        return EventHandler.super.after(persisted);
    }

    @Override
    public void onTerminate() {
        lastInstant.set(clock.instant().toString());
        lastDuration.set(getCurrentDuration());
        startNanoTime.set(null);
        EventHandler.super.onTerminate();
    }

    private String getCurrentDuration() {
        long endTime = clock.millis();
        Long startTime = startNanoTime.get();
        return Optional.ofNullable(startTime).map(st -> {
            Duration d = Duration.ofMillis(endTime - st);
            return String.format("%02ds %02dms", d.toSecondsPart(), d.toMillisPart());
        }).orElse("...");
    }

    @Override
    public CounterGroup group() {
        return CounterGroup.TECHWATCH;
    }

    @Override
    public Mono<Counter> computeCounter() {
        return Mono.just(Counter.create(
                "Scraping Duration",
                HeroIcons.CloudArrowUpIcon,
                lastDuration.get(),
                lastInstant.get()));
    }

    @Override
    public Set<String> eventTypes() {
        return Set.of("FEED_SCRAPING");
    }
}
