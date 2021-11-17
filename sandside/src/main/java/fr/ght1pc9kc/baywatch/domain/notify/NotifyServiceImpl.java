package fr.ght1pc9kc.baywatch.domain.notify;

import fr.ght1pc9kc.baywatch.api.notify.NotifyService;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;
import reactor.core.publisher.Sinks.EmitResult;

@Slf4j
public class NotifyServiceImpl implements NotifyService {
    private final Sinks.Many<Object> sink;

    public NotifyServiceImpl() {
        this.sink = Sinks.many().multicast().directBestEffort();
    }

    @Override
    public Flux<?> getFlux() {
        return this.sink.asFlux();
    }

    @Override
    public <T> void send(T data) {
        EmitResult result = this.sink.tryEmitNext(data);
        if (result.isFailure()) {
            if (result == EmitResult.FAIL_ZERO_SUBSCRIBER) {
                log.debug("No subscriber listening the SSE entry point.");
            } else {
                log.warn("{} on emit notification {}", result, data);
            }
        }
    }
}
