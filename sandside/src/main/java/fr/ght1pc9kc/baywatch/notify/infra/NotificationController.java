package fr.ght1pc9kc.baywatch.notify.infra;

import fr.ght1pc9kc.baywatch.notify.api.NotifyManager;
import fr.ght1pc9kc.baywatch.notify.api.model.BasicEvent;
import fr.ght1pc9kc.baywatch.notify.api.model.EventType;
import fr.ght1pc9kc.baywatch.notify.api.model.ReactiveEvent;
import fr.ght1pc9kc.baywatch.notify.api.model.ServerEventVisitor;
import fr.ght1pc9kc.baywatch.security.api.AuthenticationFacade;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;
import reactor.core.publisher.Mono;

@Slf4j
@RestController
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('USER', 'MANAGER', 'ADMIN')")
@RequestMapping("${baywatch.base-route}/sse")
public class NotificationController {
    private final NotifyManager notifyManager;
    private final AuthenticationFacade facade;

    @GetMapping
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
                .map(ignore -> ResponseEntity.noContent().build())
                .switchIfEmpty(Mono.just(ResponseEntity.notFound().build()));

    }

    @GetMapping("/test")
    @PreAuthorize("hasRole('ADMIN')")
    public Mono<Void> test(@RequestParam("msg") String msg) {
        return facade.getConnectedUser().map(user -> {
            notifyManager.broadcast(EventType.NEWS_UPDATE, "UPDATE");
            notifyManager.send(user.id, EventType.USER_NOTIFICATION, "PERSO");
            return user;
        }).then();
    }
}
