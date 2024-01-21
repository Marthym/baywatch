package fr.ght1pc9kc.baywatch.techwatch.domain.ports;

import fr.ght1pc9kc.baywatch.common.api.model.EntitiesProperties;
import fr.ght1pc9kc.baywatch.techwatch.api.model.WebFeed;
import fr.ght1pc9kc.baywatch.techwatch.domain.model.QueryContext;
import fr.ght1pc9kc.baywatch.techwatch.infra.model.FeedDeletedResult;
import fr.ght1pc9kc.entity.api.Entity;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collection;

public interface FeedPersistencePort {
    Mono<Entity<WebFeed>> get(QueryContext qCtx);

    default Flux<Entity<WebFeed>> list() {
        return list(QueryContext.empty());
    }

    Flux<Entity<WebFeed>> list(QueryContext qCtx);

    Mono<Integer> count(QueryContext qCtx);

    /**
     * Allow user to update {@link WebFeed} name or tags.
     * URL was immutable. The Feed was shared and the URL give the ID so the URL was not updatabel.
     *
     * @param id       The ID of the {@link WebFeed} to update
     * @param userId   The user id used to update
     * @param toUpdate The {@link WebFeed} to persist
     * @return The new updated {@link WebFeed}
     */
    Mono<Entity<WebFeed>> update(String id, String userId, WebFeed toUpdate);

    /**
     * Persist one or more {@link WebFeed}s into {@code FEED} table
     *
     * @param toPersist the list of Feed to persist
     * @return The persisted Feed
     */
    Flux<Entity<WebFeed>> persist(Collection<WebFeed> toPersist);

    /**
     * <p>Link a {@link WebFeed} to a {@link fr.ght1pc9kc.baywatch.security.api.model.User} by add lines into
     * {@code FEEDS_USERS} relation table.</p>
     * <p>The {@link WebFeed} object was used to override the Feed Name or Tags if necessary.</p>
     *
     * @param feedsIds The list of Feed to link with user
     * @param userId   The user ID
     * @return The list of Feeds IDs linked to the User
     */
    Flux<Entity<WebFeed>> persistUserRelation(Collection<WebFeed> feedsIds, String userId);

    /**
     * Delete {@link WebFeed} from {@code FEEDS_USERS} and {@code FEEDS}, depending on the filter in {@link QueryContext}.
     * The deletion was scoped to the {@link fr.ght1pc9kc.baywatch.security.api.model.User} is provided in
     * {@link QueryContext}.
     * <p>Allowed properties was:</p>
     * <ul>
     *    <li>{@link EntitiesProperties#ID}: For the table {@code FEEDS}</li>
     *    <li>{@link EntitiesProperties#FEED_ID}: For the table {@code FEEDS_USERS}</li>
     * </ul>
     * <p>Deletion was apply only if a filter was present for a table.</p>
     * <p>If no other {@link fr.ght1pc9kc.baywatch.security.api.model.User} was subscribing this {@link WebFeed},
     * The {@link WebFeed} was deleted with all the `NEWS_FEED` relations.</p>
     *
     * @param qCtx Context of the query, containing the filter.
     * @return The number of feed effectively deleted
     */
    Mono<FeedDeletedResult> delete(QueryContext qCtx);
}
