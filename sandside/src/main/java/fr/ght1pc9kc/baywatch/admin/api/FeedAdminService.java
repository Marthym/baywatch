package fr.ght1pc9kc.baywatch.admin.api;

import fr.ght1pc9kc.baywatch.techwatch.api.model.Feed;
import fr.ght1pc9kc.baywatch.techwatch.api.model.RawFeed;
import fr.ght1pc9kc.baywatch.security.api.model.User;
import fr.ght1pc9kc.juery.api.PageRequest;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collection;

public interface FeedAdminService {
    Mono<RawFeed> get(String id);

    Flux<RawFeed> list();

    /**
     * List {@link Feed} independently of the {@link User} or any other entity.
     *
     * @param pageRequest The query parameters
     * @return The {@link RawFeed} version of the {@link Feed}
     */
    Flux<RawFeed> list(PageRequest pageRequest);

    Mono<Integer> delete(Collection<String> toDelete);
}