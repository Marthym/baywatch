package fr.ght1pc9kc.baywatch.infra.notify;

import fr.ght1pc9kc.baywatch.api.notify.NotifyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
@RequiredArgsConstructor
@RequestMapping("${baywatch.base-route}/sse")
public class NotificationController {
    private final NotifyService notifyService;

    @GetMapping(produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<Object>> sse() {
        return notifyService.getFlux().map(e -> ServerSentEvent.<Object>builder(e).build());
    }
}
