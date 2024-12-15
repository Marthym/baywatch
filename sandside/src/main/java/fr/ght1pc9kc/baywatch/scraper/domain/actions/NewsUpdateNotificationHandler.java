package fr.ght1pc9kc.baywatch.scraper.domain.actions;

import fr.ght1pc9kc.baywatch.notify.api.NotifyService;
import fr.ght1pc9kc.baywatch.notify.api.model.EventType;
import fr.ght1pc9kc.baywatch.scraper.api.ScrapingEventHandler;
import fr.ght1pc9kc.baywatch.scraper.api.model.ScrapResult;
import fr.ght1pc9kc.baywatch.scraper.api.model.ScrapingEventType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

import java.util.EnumSet;

@Slf4j
@RequiredArgsConstructor
public class NewsUpdateNotificationHandler implements ScrapingEventHandler {
    private final NotifyService notifyService;

    @Override
    public Mono<Void> after(ScrapResult result) {
        return Mono.just(result.inserted())
                .filter(p -> p > 0)
                .map(p -> {
                    notifyService.broadcast(EventType.NEWS_UPDATE, "UPDATED");
                    return p;
                }).then();
    }

    @Override
    public EnumSet<ScrapingEventType> eventTypes() {
        return EnumSet.of(ScrapingEventType.FEED_SCRAPING);
    }
}
