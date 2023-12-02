package fr.ght1pc9kc.baywatch.scraper.infra.controllers;

import fr.ght1pc9kc.baywatch.scraper.api.FeedScraperService;
import fr.ght1pc9kc.baywatch.scraper.infra.config.ScraperApplicationProperties;
import fr.ght1pc9kc.baywatch.security.api.AuthenticationFacade;
import jakarta.annotation.PreDestroy;
import lombok.AccessLevel;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.VisibleForTesting;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@ConditionalOnProperty(prefix = "baywatch.scraper", name = "enable", havingValue = "true", matchIfMissing = true)
public class ScraperTaskScheduler implements Runnable {
    public static final String ERROR_CLASS_MESSAGE = "{}: {}";
    public static final String ERROR_STACKTRACE_MESSAGE = "STACKTRACE";

    private final ScheduledExecutorService scheduleExecutor = Executors.newSingleThreadScheduledExecutor(
            new CustomizableThreadFactory("scrapSched-"));
    private final Semaphore lock = new Semaphore(1);

    private final FeedScraperService scraperService;
    private final ScraperApplicationProperties properties;

    public ScraperTaskScheduler(FeedScraperService scraperService, ScraperApplicationProperties properties) {
        this.scraperService = scraperService;
        this.properties = properties;
    }

    @Setter(value = AccessLevel.PACKAGE, onMethod = @__({@VisibleForTesting}))
    private Clock clock = Clock.systemUTC();

    @Async
    @EventListener(ApplicationStartedEvent.class)
    public void startScrapping() {
        Instant now = clock.instant();
        Instant nextScrapping = now.plus(properties.frequency());
        Duration toNextScrapping = Duration.between(now, nextScrapping);

        scheduleExecutor.scheduleAtFixedRate(this,
                toNextScrapping.getSeconds(), properties.frequency().getSeconds(), TimeUnit.SECONDS);
        log.debug("Next scraping at {}", LocalDateTime.now(clock).plus(toNextScrapping));
        scheduleExecutor.schedule(this, 0, TimeUnit.MILLISECONDS);
    }

    @PreDestroy
    @SneakyThrows
    public void shutdownScrapping() {
        if (!lock.tryAcquire(60, TimeUnit.SECONDS)) {
            log.warn("Unable to stop threads gracefully ! Threads was killed !");
        }
        scraperService.dispose();
        scheduleExecutor.shutdownNow();
        lock.release();
        log.info("All scraper tasks finished and stopped !");
    }

    @VisibleForTesting
    public boolean isScraping() {
        return lock.availablePermits() == 0;
    }

    @Override
    public void run() {
        if (!lock.tryAcquire()) {
            log.warn("Scraping in progress !");
            return;
        }
        log.info("Start scraping ...");
        scraperService.scrap(properties.conservation())
                .doOnError(e -> {
                    log.error(ERROR_CLASS_MESSAGE, e.getClass(), e.getLocalizedMessage());
                    log.debug(ERROR_STACKTRACE_MESSAGE, e);
                })
                .doFinally(signal -> {
                    lock.release();
                    log.debug("Scraping terminated successfully !");
                })
                .contextWrite(AuthenticationFacade.withSystemAuthentication())
                .subscribe();
    }
}
