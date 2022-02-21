package fr.ght1pc9kc.baywatch.notify.api;

import reactor.core.publisher.Mono;

public record ReactiveEvent<T>(
        String id,
        EventType type,
        Mono<T> message
) implements ServerEvent<T> {
    @Override
    public <R> R accept(ServerEventVisitor<R> visitor) {
        return visitor.visit(this);
    }
}
