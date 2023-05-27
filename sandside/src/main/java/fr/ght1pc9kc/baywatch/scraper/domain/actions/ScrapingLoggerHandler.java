package fr.ght1pc9kc.baywatch.scraper.domain.actions;

import fr.ght1pc9kc.baywatch.common.api.ScrapingEventHandler;
import fr.ght1pc9kc.baywatch.scraper.api.model.ScrapResult;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

import java.util.Set;

@Slf4j
public class ScrapingLoggerHandler implements ScrapingEventHandler {
    @Override
    public Mono<Void> after(ScrapResult result) {
        return Mono.just(result).map(r -> {
            log.info("Scraping finished, {} news inserted, {} error(s).", result.inserted(), result.errors().size());
            result.errors().forEach(se -> {
                log.warn("{} => {}: {}", se.link(), se.exception().getClass(), se.exception().getLocalizedMessage());
                if (log.isDebugEnabled()) {
                    log.debug("STACKTRACE", se.exception());
                }
            });
            return result;
        }).then();
    }

    @Override
    public Set<String> eventTypes() {
        return Set.of("FEED_SCRAPING");
    }
}
