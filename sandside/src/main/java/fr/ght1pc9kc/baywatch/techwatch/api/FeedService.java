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

    /**
     * Update the subscription to a {@link Feed}
     *
     * @param toPersist the Feed of the subscription to update
     * @return The new Feed of the subscription
     */
    Mono<Feed> update(Feed toPersist);

    /**
     * Add a {@link Feed} to the available Feed list in database
     *
     * @param toAdd The list of {@link Feed} to add
     * @return The list of  {@link Feed} added
     */
    Flux<Feed> add(Collection<Feed> toAdd);

    /**
     * Subscribe to a {@link Feed} present in database
     *
     * @param feeds The list of feed IDs the current user want to subscribe
     * @return The {@link Feed} the user have effectively subscribed
     */
    Flux<Feed> subscribe(Collection<Feed> feeds);

    Flux<Feed> addAndSubscribe(Collection<Feed> feeds);

    Mono<Integer> delete(Collection<String> toDelete);
}
