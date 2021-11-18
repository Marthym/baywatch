package fr.ght1pc9kc.baywatch.domain.notify;

import fr.ght1pc9kc.baywatch.api.notify.EventType;
import fr.ght1pc9kc.baywatch.api.notify.NotifyService;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;
import reactor.core.publisher.Sinks.EmitResult;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

@Slf4j
public class NotifyServiceImpl implements NotifyService {
    private final Sinks.Many<Tuple2<EventType, Object>> sink;

    public NotifyServiceImpl() {
        this.sink = Sinks.many().multicast().directBestEffort();
    }

    @Override
    public Flux<Tuple2<EventType, Object>> getFlux() {
        return this.sink.asFlux();
    }

    @Override
    public <T> void send(EventType type, T data) {
        EmitResult result = this.sink.tryEmitNext(Tuples.of(type, data));
        if (result.isFailure()) {
            if (result == EmitResult.FAIL_ZERO_SUBSCRIBER) {
                log.debug("No subscriber listening the SSE entry point.");
            } else {
                log.warn("{} on emit notification {}", result, data);
            }
        }
    }
}
