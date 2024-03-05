package fr.ght1pc9kc.baywatch.techwatch.api;

import fr.ght1pc9kc.baywatch.security.api.model.User;
import fr.ght1pc9kc.baywatch.techwatch.api.model.News;
import fr.ght1pc9kc.baywatch.techwatch.api.model.RawNews;
import fr.ght1pc9kc.baywatch.techwatch.api.model.State;
import fr.ght1pc9kc.baywatch.techwatch.api.model.WebFeed;
import fr.ght1pc9kc.entity.api.Entity;
import fr.ght1pc9kc.juery.api.PageRequest;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collection;

public interface SystemMaintenanceService {
    /**
     * List {@link WebFeed} independently of the {@link User} or any other entity
     *
     * @return The {@link WebFeed} version of the feed
     */
    Flux<Entity<WebFeed>> feedList();

    /**
     * List {@link WebFeed} independently of the {@link User} or any other entity.
     *
     * @param pageRequest The query parameters
     * @return The {@link WebFeed} version of the feed
     */
    Flux<Entity<WebFeed>> feedList(PageRequest pageRequest);

    Mono<Integer> feedDelete(Collection<String> toDelete);

    Mono<Void> feedsUpdate(Collection<Entity<WebFeed>> toUpdate);

    /**
     * List {@link News} for connected user or {@link News} for anonymous.
     * For Anonymous, {@link State} is always
     * {@link State#NONE}
     *
     * @param pageRequest {@see PageRequest}
     * @return The {@link News} for connected user or {@link News} for anonymous
     */
    Flux<News> newsList(PageRequest pageRequest);

    Flux<String> newsIdList(PageRequest pageRequest);

    Mono<Integer> newsLoad(Collection<News> toLoad);

    /**
     * Delete {@link RawNews} from the database
     *
     * @param toDelete ID of the {@link RawNews}s to delete
     * @return The number of {@link RawNews} effectively deleted
     */
    Mono<Integer> newsDelete(Collection<String> toDelete);
}
