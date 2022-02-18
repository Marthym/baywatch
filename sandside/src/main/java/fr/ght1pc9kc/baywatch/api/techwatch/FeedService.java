package fr.ght1pc9kc.baywatch.api.techwatch;

import fr.ght1pc9kc.baywatch.api.techwatch.model.Feed;
import fr.ght1pc9kc.baywatch.api.techwatch.model.RawFeed;
import fr.ght1pc9kc.baywatch.api.security.model.User;
import fr.ght1pc9kc.juery.api.PageRequest;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collection;

public interface FeedService {
    Mono<Feed> get(String id);

    Flux<Feed> list();

    Flux<Feed> list(PageRequest pageRequest);

    /**
     * Count the total number of elements returned by the {@link PageRequest}
     * ignoring {@link fr.ght1pc9kc.juery.api.Pagination}
     *
     * @param pageRequest The page request with filters
     * @return The total elements count
     */
    Mono<Integer> count(PageRequest pageRequest);

    /**
     * List {@link Feed} independently of the {@link User} or any other entity.
     *
     * @param pageRequest The query parameters
     * @return The {@link RawFeed} version of the {@link Feed}
     */
    Flux<RawFeed> raw(PageRequest pageRequest);

    Mono<Feed> update(Feed toPersist);

    Mono<Void> persist(Collection<Feed> toPersist);

    Mono<Integer> delete(Collection<String> toDelete);
}
