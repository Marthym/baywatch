package fr.ght1pc9kc.baywatch.security.infra.adapters;

import fr.ght1pc9kc.baywatch.security.api.model.User;
import fr.ght1pc9kc.baywatch.security.domain.ports.UserEventPublisherPort;
import fr.ght1pc9kc.entity.api.Entity;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

@Component
public class UserEventPublisherAdapter implements UserEventPublisherPort {
    private final Sinks.Many<Entity<User>> sink = Sinks.many().multicast().onBackpressureBuffer();

    public Mono<Entity<User>> publish(Entity<User> user) {
        sink.tryEmitNext(user);
        return Mono.just(user);
    }

    public Flux<Entity<User>> events() {
        return sink.asFlux();
    }
}
