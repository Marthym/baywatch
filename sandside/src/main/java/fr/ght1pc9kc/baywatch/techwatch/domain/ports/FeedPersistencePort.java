package fr.ght1pc9kc.baywatch.techwatch.domain.ports;

import fr.ght1pc9kc.baywatch.common.api.model.EntitiesProperties;
import fr.ght1pc9kc.baywatch.techwatch.api.model.Feed;
import fr.ght1pc9kc.baywatch.techwatch.domain.model.QueryContext;
import fr.ght1pc9kc.baywatch.techwatch.infra.model.FeedDeletedResult;
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

    /**
     * Persist one or more {@link Feed}s into {@code FEED} table
     *
     * @param toPersist the list of Feed to persist
     * @return The persisted Feed
     */
    Flux<Feed> persist(Collection<Feed> toPersist);

    /**
     * <p>Link a {@link Feed} to a {@link fr.ght1pc9kc.baywatch.security.api.model.User} by add lines into
     * {@code FEEDS_USERS} relation table.</p>
     * <p>The {@link Feed} object was used to override the Feed Name or Tags if necessary.</p>
     *
     * @param feedsIds The list of Feed to link with user
     * @param userId   The user ID
     * @return The list of Feeds IDs linked to the User
     */
    Flux<Feed> persistUserRelation(Collection<Feed> feedsIds, String userId);

    /**
     * Delete {@link Feed} from {@code FEEDS_USERS} and {@code FEEDS}, depending on the filter in {@link QueryContext}.
     * The deletion was scoped to the {@link fr.ght1pc9kc.baywatch.security.api.model.User} is provided in
     * {@link QueryContext}.
     * <p>Allowed properties was:</p>
     * <ul>
     *    <li>{@link EntitiesProperties#ID}: For the table {@code FEEDS}</li>
     *    <li>{@link EntitiesProperties#FEED_ID}: For the table {@code FEEDS_USERS}</li>
     * </ul>
     * <p>Deletion was apply only if a filter was present for a table.</p>
     * <p>If no other {@link fr.ght1pc9kc.baywatch.security.api.model.User} was subscribing this {@link Feed},
     * The {@link Feed} was deleted with all the `NEWS_FEED` relations.</p>
     *
     * @param qCtx Context of the query, containing the filter.
     * @return The number of feed effectively deleted
     */
    Mono<FeedDeletedResult> delete(QueryContext qCtx);
}
