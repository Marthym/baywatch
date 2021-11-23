package fr.ght1pc9kc.baywatch.domain.scrapper.actions;

import fr.ght1pc9kc.baywatch.api.StatService;
import fr.ght1pc9kc.baywatch.api.notify.EventType;
import fr.ght1pc9kc.baywatch.api.notify.NotifyService;
import fr.ght1pc9kc.baywatch.api.scrapper.ScrappingHandler;
import fr.ght1pc9kc.baywatch.infra.model.Statistics;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
@RequiredArgsConstructor
public class NewsUpdateNotificationHandler implements ScrappingHandler {
    private final NotifyService notifyService;
    private final StatService statService;

    @Override
    public Mono<Void> after(int persisted) {
        return Mono.just(persisted)
                .filter(p -> p > 0)
                .map(p -> {
                    Mono<Statistics> stats = Mono.zip(
                                    statService.getFeedsCount(),
                                    statService.getNewsCount(),
                                    statService.getUnreadCount())
                            .map(t -> Statistics.builder()
                                    .feeds(t.getT1())
                                    .news(t.getT2())
                                    .unread(t.getT3())
                                    .build());
                    notifyService.send(EventType.NEWS, stats);
                    return p;
                }).then();
    }
}
