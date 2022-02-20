package fr.ght1pc9kc.baywatch.scrapper.domain.actions;

import fr.ght1pc9kc.baywatch.techwatch.api.StatService;
import fr.ght1pc9kc.baywatch.notify.api.EventType;
import fr.ght1pc9kc.baywatch.notify.api.NotifyService;
import fr.ght1pc9kc.baywatch.scrapper.api.ScrappingHandler;
import fr.ght1pc9kc.baywatch.techwatch.infra.model.Statistics;
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
