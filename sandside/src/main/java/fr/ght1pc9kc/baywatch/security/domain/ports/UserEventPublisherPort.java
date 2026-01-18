package fr.ght1pc9kc.baywatch.security.domain.ports;

import fr.ght1pc9kc.baywatch.security.api.model.User;
import fr.ght1pc9kc.entity.api.Entity;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface UserEventPublisherPort {
    Mono<Entity<User>> publish(Entity<User> user);

    Flux<Entity<User>> events();
}
