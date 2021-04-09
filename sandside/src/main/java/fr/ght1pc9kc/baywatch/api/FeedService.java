package fr.ght1pc9kc.baywatch.api;

import fr.ght1pc9kc.baywatch.api.model.Feed;
import fr.ght1pc9kc.baywatch.api.model.request.PageRequest;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collection;

public interface FeedService {
    Mono<Feed> get(String id);

    Flux<Feed> list();

    Flux<Feed> list(PageRequest pageRequest);

    Mono<Void> persist(Collection<Feed> toPersist);

    Mono<Integer> delete(Collection<String> toDelete);
}
