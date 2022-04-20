package fr.ght1pc9kc.baywatch.scrapper.infra.adapters;

import fr.ght1pc9kc.baywatch.admin.api.model.Counter;
import fr.ght1pc9kc.baywatch.admin.api.model.CounterProvider;
import fr.ght1pc9kc.baywatch.admin.api.model.CounterType;
import fr.ght1pc9kc.baywatch.scrapper.api.ScrappingHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
@Component
public class ScrapingDurationCounter implements CounterProvider, ScrappingHandler {
    private final AtomicReference<Long> startNanoTime = new AtomicReference<>(null);
    private final AtomicReference<String> lastDuration = new AtomicReference<>("...");
    private final AtomicReference<String> lastInstant = new AtomicReference<>("scraping in progress...");

    @Override
    public Mono<Void> before() {
        startNanoTime.set(System.nanoTime());
        return ScrappingHandler.super.before();
    }

    @Override
    public Mono<Void> after(int persisted) {
        lastInstant.set(DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(Instant.now()));
        long endTime = System.nanoTime();
        Long startTime = startNanoTime.getAndSet(null);
        String duration = Optional.ofNullable(startTime).map(st -> {
            Duration d = Duration.ofNanos(endTime - st);
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
