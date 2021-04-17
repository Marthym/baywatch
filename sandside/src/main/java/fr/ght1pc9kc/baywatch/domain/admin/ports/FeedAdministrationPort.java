package fr.ght1pc9kc.baywatch.domain.admin.ports;

import fr.ght1pc9kc.baywatch.api.model.Feed;
import fr.ght1pc9kc.baywatch.api.model.RawFeed;
import fr.ght1pc9kc.baywatch.api.model.request.PageRequest;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collection;

public interface FeedAdministrationPort {
    Mono<RawFeed> get(String id);

    Flux<RawFeed> list();

    Flux<RawFeed> list(PageRequest pageRequest);

    /**
     * Delete {@link Feed} for all the users and finally delete the {@link Feed} it self
     * The {@link Feed} must not have {@link fr.ght1pc9kc.baywatch.api.model.News} linked with.
     *
     * @param toDelete The feed IDs to remove
     * @return The number of feed effectively deleted
     */
    Mono<Integer> delete(Collection<String> toDelete);
}
