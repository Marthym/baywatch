package fr.ght1pc9kc.baywatch.notify.infra;

import fr.ght1pc9kc.baywatch.notify.api.BasicEvent;
import fr.ght1pc9kc.baywatch.notify.api.EventType;
import fr.ght1pc9kc.baywatch.notify.api.NotifyManager;
import fr.ght1pc9kc.baywatch.notify.api.NotifyService;
import fr.ght1pc9kc.baywatch.notify.api.ReactiveEvent;
import fr.ght1pc9kc.baywatch.notify.api.ServerEventVisitor;
import fr.ght1pc9kc.baywatch.techwatch.api.StatService;
import fr.ght1pc9kc.baywatch.techwatch.infra.model.Statistics;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@RestController
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('USER', 'MANAGER', 'ADMIN')")
@RequestMapping("${baywatch.base-route}/sse")
public class NotificationController {
    private final NotifyManager notifyManager;
    private final NotifyService notifyService;
    private final StatService statService;

    private int userBidon = 0;

    @GetMapping(produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<Object>> sse() {
        return notifyManager.subscribe().flatMap(evt ->
                evt.accept(new ServerEventVisitor<Mono<?>>() {
                            @Override
                            public <T> Mono<T> visit(BasicEvent<T> event) {
                                return Mono.just(event.message());
                            }

                            @Override
                            public <T> Mono<T> visit(ReactiveEvent<T> event) {
                                return event.message();
                            }
                        })
                        .map(msg -> ServerSentEvent.builder()
                                .id(evt.id())
                                .event(evt.type().getName()).data(msg)
                                .build()));
    }

    @DeleteMapping
    public Mono<ResponseEntity<Object>> disposeSse() {
        return notifyManager.unsubscribe()
                .map(_x -> ResponseEntity.noContent().build())
                .switchIfEmpty(Mono.just(ResponseEntity.notFound().build()));

    }

    @GetMapping("/test")
    @PreAuthorize("hasRole('ADMIN')")
    public Mono<Void> test() {
        ++userBidon;
        Mono<Statistics> stats = Mono.zip(
                statService.getFeedsCount(),
                statService.getNewsCount(),
                statService.getUnreadCount()
        ).map(s -> Statistics.builder()
                .feeds(s.getT1())
                .news(s.getT2() + 10)
                .unread(s.getT3() + 10)
                .users(userBidon).build());
        notifyService.send(EventType.NEWS, stats);
        return Mono.empty().then();
    }
}
