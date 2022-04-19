package fr.ght1pc9kc.baywatch.scrapper.domain.actions;

import fr.ght1pc9kc.baywatch.notify.api.NotifyService;
import fr.ght1pc9kc.baywatch.notify.api.model.EventType;
import fr.ght1pc9kc.baywatch.scrapper.api.ScrappingHandler;
import fr.ght1pc9kc.baywatch.techwatch.api.NewsService;
import fr.ght1pc9kc.juery.api.PageRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
@RequiredArgsConstructor
public class NewsUpdateNotificationHandler implements ScrappingHandler {
    private final NotifyService notifyService;
    private final NewsService newsService;

    @Override
    public Mono<Void> after(int persisted) {
        return Mono.just(persisted)
                .filter(p -> p > 0)
                .map(p -> {
                    notifyService.send(EventType.NEWS, newsService.count(PageRequest.all()));
                    return p;
                }).then();
    }
}
