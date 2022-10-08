package fr.ght1pc9kc.baywatch.scraper.domain.actions;

import fr.ght1pc9kc.baywatch.common.api.EventHandler;
import fr.ght1pc9kc.baywatch.notify.api.NotifyService;
import fr.ght1pc9kc.baywatch.notify.api.model.EventType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

import java.util.Set;

@Slf4j
@RequiredArgsConstructor
public class NewsUpdateNotificationHandler implements EventHandler {
    private final NotifyService notifyService;

    @Override
    public Mono<Void> after(int persisted) {
        return Mono.just(persisted)
                .filter(p -> p > 0)
                .map(p -> {
                    notifyService.broadcast(EventType.NEWS_UPDATE, "UPDATED");
                    return p;
                }).then();
    }

    @Override
    public Set<String> eventTypes() {
        return Set.of("FEED_SCRAPING");
    }
}
