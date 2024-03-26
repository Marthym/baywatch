package fr.ght1pc9kc.baywatch.scraper.domain.actions;

import fr.ght1pc9kc.baywatch.scraper.api.ScrapingEventHandler;
import fr.ght1pc9kc.baywatch.scraper.api.model.ScrapResult;
import fr.ght1pc9kc.baywatch.scraper.api.model.ScrapingEventType;
import fr.ght1pc9kc.baywatch.scraper.domain.model.ex.FeedScrapingException;
import fr.ght1pc9kc.baywatch.scraper.domain.model.ex.NewsScrapingException;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

import java.util.EnumSet;

@Slf4j
public class ScrapingLoggerHandler implements ScrapingEventHandler {
    @Override
    public Mono<Void> after(ScrapResult result) {
        return Mono.just(result).map(r -> {
            log.info("Scraping finished, {} news inserted, {} error(s).", result.inserted(), result.errors().size());
            result.errors().forEach(se -> {
                if (se instanceof FeedScrapingException fse) {
                    log.warn("{} => {}: {}", fse.getEntity().link(), fse.getClass(), fse.getLocalizedMessage());
                } else if (se instanceof NewsScrapingException nse) {
                    log.warn("{} => {}: {}", nse.getEntity().link(), nse.getClass(), nse.getLocalizedMessage());
                } else {
                    log.warn("UNKNOWN => {}: {}", se.getClass(), se.getLocalizedMessage());
                }
                if (log.isDebugEnabled()) {
                    log.debug("STACKTRACE", se);
                }
            });
            return result;
        }).then();
    }

    @Override
    public EnumSet<ScrapingEventType> eventTypes() {
        return EnumSet.of(ScrapingEventType.FEED_SCRAPING);
    }
}
