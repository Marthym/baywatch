package fr.ght1pc9kc.baywatch.api;

import fr.ght1pc9kc.baywatch.api.model.Feed;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collection;

public interface FeedPersistencePort {
    Flux<Feed> list();

    Mono<Void> persist(Collection<Feed> toCreate);
}
