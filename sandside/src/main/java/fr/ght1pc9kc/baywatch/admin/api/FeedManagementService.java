package fr.ght1pc9kc.baywatch.admin.api;

import fr.ght1pc9kc.baywatch.admin.api.model.RawFeed;
import fr.ght1pc9kc.entity.api.Entity;
import fr.ght1pc9kc.juery.api.PageRequest;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collection;

/**
 * Administrate all application feeds
 */
public interface FeedManagementService {
    /**
     * Get raw feed by ID
     *
     * @param id The id of raw feed requested
     * @return The corresponding raw feed
     */
    Mono<Entity<RawFeed>> get(String id);

    /**
     * Lookup for one or more raw feed.
     *
     * @param pageRequest Filters and pagination request
     * @return The raw feed corresponding to the request
     */
    Flux<Entity<RawFeed>> find(PageRequest pageRequest);

    /**
     * Create a new raw feed
     *
     * @param rawFeed raw feed details
     * @return The new raw feed entity with ID
     */
    Mono<Entity<RawFeed>> create(RawFeed rawFeed);

    /**
     * Update an existing raw feed
     *
     * @param id      The ID of the raw feed to update
     * @param rawFeed The news details of the raw feed to update
     * @return The updated raw feed Entity
     */
    Mono<Entity<RawFeed>> update(String id, RawFeed rawFeed);

    /**
     * Delete existing raw feed(s)
     *
     * @param ids ID of the raw feeds to be deleted
     * @return Nothing when deletion complete
     */
    Mono<Void> delete(Collection<String> ids);
}
