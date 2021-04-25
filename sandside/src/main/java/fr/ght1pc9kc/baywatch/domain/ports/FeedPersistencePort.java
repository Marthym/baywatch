package fr.ght1pc9kc.baywatch.domain.ports;

import fr.ght1pc9kc.baywatch.api.model.Feed;
import fr.ght1pc9kc.juery.api.PageRequest;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collection;

public interface FeedPersistencePort {
    Mono<Feed> get(String id);

    Flux<Feed> list();

    Flux<Feed> list(PageRequest pageRequest);

    Mono<Void> persist(Collection<Feed> toPersist);

    Mono<Void> persist(Collection<Feed> toPersist, String userId);

    /**
     * Delete {@link Feed} for all the users. Only the link between {@link Feed} and users
     * was deleted. Use {@link fr.ght1pc9kc.baywatch.domain.admin.ports.FeedAdministrationPort} to delete
     * {@link Feed} completely.
     *
     * @param toDelete The feed IDs to remove
     * @return The number of feed effectively deleted
     */
    Mono<Integer> delete(Collection<String> toDelete);

    /**
     * Delete {@link Feed} for a specific user. Only the link between {@link Feed} and the user
     * was deleted. If the user was the only owner for the {@link Feed}, the {@link Feed} will
     * be deleted at next purge time.
     *
     * @param toDelete The feed IDs to remove
     * @param userId   The ID of the concerned user
     * @return The number of feed effectively deleted
     */
    Mono<Integer> delete(Collection<String> toDelete, String userId);
}
