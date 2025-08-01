package fr.ght1pc9kc.baywatch.security.domain.ports;

import fr.ght1pc9kc.baywatch.security.api.model.User;
import fr.ght1pc9kc.baywatch.security.domain.model.PersonalFeed;
import fr.ght1pc9kc.entity.api.Entity;
import reactor.core.publisher.Mono;

public interface TechwatchModulePort {
    Mono<Void> addAndSubscribePersonalFeed(PersonalFeed personalFeed);

    Mono<Void> unsubscribePersonalFeed(Entity<User> user);

    Mono<Void> deletePersonalFeed(Entity<User> user);
}
