package fr.ght1pc9kc.baywatch.notify.infra;

import com.github.f4b6a3.ulid.UlidCreator;
import fr.ght1pc9kc.baywatch.notify.api.EventType;
import fr.ght1pc9kc.baywatch.notify.api.NotifyService;
import fr.ght1pc9kc.baywatch.security.domain.ports.AuthenticationFacade;
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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@RestController
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('USER', 'MANAGER', 'ADMIN')")
@RequestMapping("${baywatch.base-route}/sse")
public class NotificationController {
    private final NotifyService notifyService;
    private final StatService statService;
    private final AuthenticationFacade authenticationFacade;

    private int userBidon = 0;

    private final Map<String, Flux<ServerSentEvent<Object>>> subscriptions = new ConcurrentHashMap<>();

    @GetMapping(produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<Object>> sse() {
        return authenticationFacade.getConnectedUser()
                .flatMapMany(u -> subscriptions.computeIfAbsent(u.id, id ->
                        notifyService.getFlux()
                                .takeWhile(e -> subscriptions.containsKey(id))
                                .flatMap(e -> e.getT2().map(s -> ServerSentEvent.builder()
                                        .id(UlidCreator.getMonotonicUlid().toString())
                                        .event(e.getT1().getName()).data(s)
                                        .build())
                                ).map(e -> {
                                    log.debug("Event: {}", e);
                                    return e;
                                }).cache(0)
                ));
    }

    @DeleteMapping
    public Mono<ResponseEntity<Object>> disposeSse() {
        return authenticationFacade.getConnectedUser()
                .filter(u -> subscriptions.containsKey(u.id))
                .map(u -> {
                    log.debug("Dispose SSE Subscription for {}", u.id);
                    return subscriptions.remove(u.id);
                })
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
