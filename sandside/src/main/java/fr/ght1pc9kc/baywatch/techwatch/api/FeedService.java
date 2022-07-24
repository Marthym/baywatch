package fr.ght1pc9kc.baywatch.techwatch.api;

import fr.ght1pc9kc.baywatch.techwatch.api.model.Feed;
import fr.ght1pc9kc.juery.api.PageRequest;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collection;

public interface FeedService {
    /**
     * Get a single {@link Feed}
     *
     * @param id the ID of the requested Feed
     * @return The Feed
     */
    Mono<Feed> get(String id);

    /**
     * List all the {@link Feed} of the connected user
     *
     * @return a Flux of Feed
     */
    Flux<Feed> list();

    /**
     * Depending on the filters this return the list of news, scoped by the current user.
     * If specifics feeds ID was asked, the list was not scoped to the user, it has no sens.
     *
     * @param pageRequest The page request
     * @return The list of feeds
     */
    Flux<Feed> list(PageRequest pageRequest);

    /**
     * Count the total number of elements returned by the {@link PageRequest}
     * ignoring {@link fr.ght1pc9kc.juery.api.Pagination}
     * If specifics feeds ID was asked, the count was not limited to the user's Feed, it has no sens.
     *
     * @param pageRequest The page request with filters
     * @return The total elements count
     */
    Mono<Integer> count(PageRequest pageRequest);

    Mono<Feed> update(Feed toPersist);

    Mono<Void> persist(Collection<Feed> toPersist);

    Mono<Integer> delete(Collection<String> toDelete);
}
