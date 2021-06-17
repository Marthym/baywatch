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
     * Delete {@link Feed} from `FEEDS_USERS` and `FEEDS`, depending on the filter in {@link QueryContext}.
     * The deletion was scoped to the {@link fr.ght1pc9kc.baywatch.api.security.model.User} is provided in
     * {@link QueryContext}.
     * <p>Allowed properties was:</p>
     * <ul>
     *    <li>{@link fr.ght1pc9kc.baywatch.api.model.EntitiesProperties#ID}: For the table `FEEDS`</li>
     *    <li>{@link fr.ght1pc9kc.baywatch.api.model.EntitiesProperties#FEED_ID}: For the table `FEEDS_USERS`</li>
     * </ul>
     * <p>Deletion was apply only if a filter was present for a table.</p>
     *
     * @param qCtx Context of the query, containing the filter.
     * @return The number of feed effectively deleted
     */
    Mono<Integer> delete(QueryContext qCtx);
}
