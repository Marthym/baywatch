package fr.ght1pc9kc.baywatch.scrapper.infra.adapters;

import fr.ght1pc9kc.baywatch.admin.api.model.Counter;
import fr.ght1pc9kc.baywatch.admin.api.model.CounterProvider;
import fr.ght1pc9kc.baywatch.admin.api.model.CounterType;
import fr.ght1pc9kc.baywatch.scrapper.api.ScrappingHandler;
import lombok.AccessLevel;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.VisibleForTesting;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.Clock;
import java.time.Duration;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
@Component
public class ScrapingDurationCounter implements CounterProvider, ScrappingHandler {
    private final AtomicReference<Long> startNanoTime = new AtomicReference<>(null);
    private final AtomicReference<String> lastDuration = new AtomicReference<>("...");
    private final AtomicReference<String> lastInstant = new AtomicReference<>("scraping in progress...");

    @VisibleForTesting
    @Setter(AccessLevel.PACKAGE)
    private Clock clock = Clock.systemUTC();

    @Override
    public Mono<Void> before() {
        startNanoTime.set(clock.millis());
        return ScrappingHandler.super.before();
    }

    @Override
    public Mono<Void> after(int persisted) {
        lastInstant.set(DateTimeFormatter.RFC_1123_DATE_TIME
                .withLocale(Locale.FRANCE)
                .withZone(ZoneOffset.UTC)
                .format(clock.instant()));
        long endTime = clock.millis();
        Long startTime = startNanoTime.getAndSet(null);
        String duration = Optional.ofNullable(startTime).map(st -> {
            Duration d = Duration.ofMillis(endTime - st);
            return String.format("%02ds %02dms", d.toSecondsPart(), d.toMillisPart());
        }).orElse("...");
        lastDuration.set(duration);
        log.debug("Scrapping finished, load {} news in {}", persisted, duration);
        return ScrappingHandler.super.after(persisted);
    }

    @Override
    public CounterType name() {
        return CounterType.SCRAPING_DURATION;
    }

    @Override
    public Mono<Counter> computeCounter() {
        return Mono.just(new Counter(
                "Scraping Duration",
                lastDuration.get(),
                lastInstant.get()));
    }
}
