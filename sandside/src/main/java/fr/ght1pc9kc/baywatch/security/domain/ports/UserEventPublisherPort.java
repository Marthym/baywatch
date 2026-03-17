package fr.ght1pc9kc.baywatch.security.domain.ports;

import fr.ght1pc9kc.baywatch.security.api.model.User;
import fr.ght1pc9kc.entity.api.Entity;
import reactor.core.Disposable;
import reactor.core.publisher.Mono;

import java.util.function.Function;

public interface UserEventPublisherPort {
    Mono<Entity<User>> publish(Entity<User> user);

    Disposable onEvent(Function<Entity<User>, Mono<Void>> mapper);
}
