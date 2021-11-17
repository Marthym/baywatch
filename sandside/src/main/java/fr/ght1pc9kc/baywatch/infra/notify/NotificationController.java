package fr.ght1pc9kc.baywatch.infra.notify;

import com.github.f4b6a3.ulid.UlidCreator;
import fr.ght1pc9kc.baywatch.api.notify.NotifyService;
import fr.ght1pc9kc.baywatch.infra.model.Statistics;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("${baywatch.base-route}/sse")
public class NotificationController {
    private final NotifyService notifyService;

    private int newsCount = 4000;
    private int unreadCount = 2000;

    @GetMapping(produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<Object>> sse() {
        return notifyService.getFlux().map(e -> ServerSentEvent.builder()
                .id(UlidCreator.getMonotonicUlid().toString())
                .event("NEWS").data(e)
                .build()).map(e -> {
                    log.debug("Event: {}", e);
                    return e;
        });
    }

    @GetMapping("/test")
    public Mono<Void> test() {
        notifyService.send(Statistics.builder()
                .feeds(17)
                .news(newsCount++)
                .unread(unreadCount++)
                .users(1).build()
        );
        return Mono.empty().then();
    }
}
