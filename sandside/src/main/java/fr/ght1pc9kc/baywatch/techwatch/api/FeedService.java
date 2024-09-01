package fr.ght1pc9kc.baywatch.techwatch.api;

import fr.ght1pc9kc.baywatch.techwatch.api.model.WebFeed;
import fr.ght1pc9kc.baywatch.techwatch.infra.model.FeedProperties;
import fr.ght1pc9kc.entity.api.Entity;
import fr.ght1pc9kc.juery.api.PageRequest;
import org.jetbrains.annotations.Nullable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collection;
import java.util.EnumSet;
import java.util.Map;

public interface FeedService {
    /**
     * Get a single {@link WebFeed}
     *
     * @param id the ID of the requested Feed
     * @return The Feed
     */
    Mono<Entity<WebFeed>> get(String id);

    /**
     * List all the {@link WebFeed} of the connected user
     *
     * @return a Flux of Feed
     */
    Flux<Entity<WebFeed>> list();

    /**
     * Depending on the filters this return the list of news, scoped by the current user.
     * If specifics feeds ID was asked, the list was not scoped to the user, it has no sens.
     *
     * @param pageRequest The page request
     * @return The list of feeds
     */
    Flux<Entity<WebFeed>> list(PageRequest pageRequest);

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
     * Update the subscription to a {@link WebFeed}
     *
     * @param toPersist the Feed of the subscription to update
     * @return The new Feed of the subscription
     */
    Mono<Entity<WebFeed>> update(Entity<WebFeed> toPersist);

    /**
     * Add a {@link WebFeed} to the available Feed list in database
     *
     * @param toAdd The list of {@link WebFeed} to add
     * @return The list of  {@link WebFeed} added
     */
    Flux<Entity<WebFeed>> add(Collection<Entity<WebFeed>> toAdd);

    /**
     * Subscribe to a {@link WebFeed} present in database
     *
     * @param feeds The list of feed IDs the current user want to subscribe
     * @return The {@link WebFeed} the user have effectively subscribed
     */
    Flux<Entity<WebFeed>> subscribe(Collection<Entity<WebFeed>> feeds);

    Flux<Entity<WebFeed>> addAndSubscribe(Collection<Entity<WebFeed>> feeds);

    Mono<Integer> delete(Collection<String> toDelete);
}
