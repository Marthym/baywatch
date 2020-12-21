package fr.ght1pc9kc.baywatch.api;

import fr.ght1pc9kc.baywatch.api.model.Feed;
import fr.ght1pc9kc.baywatch.api.model.search.Criteria;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collection;

public interface FeedPersistencePort {
    Mono<Feed> get(String id);

    Flux<Feed> list();

    Flux<Feed> list(Criteria criteria);

    Mono<Void> persist(Collection<Feed> toPersist);

    Mono<Integer> delete(Collection<Feed> toDelete);
}
