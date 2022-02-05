package fr.ght1pc9kc.baywatch.infra.notify;

import com.github.f4b6a3.ulid.UlidCreator;
import fr.ght1pc9kc.baywatch.api.StatService;
import fr.ght1pc9kc.baywatch.api.notify.EventType;
import fr.ght1pc9kc.baywatch.api.notify.NotifyService;
import fr.ght1pc9kc.baywatch.infra.model.Statistics;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.security.access.prepost.PreAuthorize;
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
    private final NotifyService notifyService;
    private final StatService statService;

    @GetMapping(produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<Object>> sse() {
        return notifyService.getFlux()
                .flatMap(e -> e.getT2().map(s -> ServerSentEvent.builder()
                        .id(UlidCreator.getMonotonicUlid().toString())
                        .event(e.getT1().getName()).data(s)
                        .build())
                ).map(e -> {
                    log.debug("Event: {}", e);
                    return e;
                });
    }

    @GetMapping("/test")
    public Mono<Void> test() {
        Mono<Statistics> stats = Mono.zip(
                statService.getFeedsCount(),
                statService.getNewsCount(),
                statService.getUnreadCount()
        ).map(s -> Statistics.builder()
                .feeds(s.getT1())
                .news(s.getT2())
                .unread(s.getT3())
                .users(1).build());
        notifyService.send(EventType.NEWS, stats);
        return Mono.empty().then();
    }
}
