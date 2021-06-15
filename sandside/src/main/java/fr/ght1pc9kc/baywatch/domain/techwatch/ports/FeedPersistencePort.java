package fr.ght1pc9kc.baywatch.domain.techwatch.ports;

import fr.ght1pc9kc.baywatch.api.model.Feed;
import fr.ght1pc9kc.baywatch.domain.techwatch.model.QueryContext;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collection;

public interface FeedPersistencePort {
    Mono<Feed> get(QueryContext qCtx);

    default Flux<Feed> list() {
        return list(QueryContext.empty());
    }

    Flux<Feed> list(QueryContext qCtx);

    Mono<Void> persist(Collection<Feed> toPersist);

    Mono<Void> persist(Collection<Feed> toPersist, String userId);

    /**
     * Delete {@link Feed} for all the users. Only the link between {@link Feed} and users
     * was deleted.
     *
     * @param qCtx Context of the query, containing the filter of the {@link Feed} to delete
     *             and the {@link fr.ght1pc9kc.baywatch.api.security.model.User}
     * @return The number of feed effectively deleted
     */
    Mono<Integer> delete(QueryContext qCtx);
}
