package fr.ght1pc9kc.baywatch.domain.techwatch.ports;

import fr.ght1pc9kc.baywatch.api.model.Feed;
import fr.ght1pc9kc.baywatch.domain.techwatch.model.QueryContext;
import fr.ght1pc9kc.baywatch.infra.model.FeedDeletedResult;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collection;

public interface FeedPersistencePort {
    Mono<Feed> get(QueryContext qCtx);

    default Flux<Feed> list() {
        return list(QueryContext.empty());
    }

    Flux<Feed> list(QueryContext qCtx);

    Mono<Integer> count(QueryContext qCtx);

    /**
     * Allow user to update {@link Feed} name or tags.
     * URL was immutable. The Feed was shared and the URL give the ID so the URL was not updatabel.
     *
     * @param toUpdate The {@link Feed} to persist
     * @param userId   The user id used to update
     * @return The new updated {@link Feed}
     */
    Mono<Feed> update(Feed toUpdate, String userId);

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
     * <p>If no other {@link fr.ght1pc9kc.baywatch.api.security.model.User} was subscribing this {@link Feed},
     * The {@link Feed} was deleted with all the `NEWS_FEED` relations.</p>
     *
     * @param qCtx Context of the query, containing the filter.
     * @return The number of feed effectively deleted
     */
    Mono<FeedDeletedResult> delete(QueryContext qCtx);
}
