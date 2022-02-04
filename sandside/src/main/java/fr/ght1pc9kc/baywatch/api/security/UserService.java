package fr.ght1pc9kc.baywatch.api.security;

import fr.ght1pc9kc.baywatch.api.security.model.User;
import fr.ght1pc9kc.juery.api.PageRequest;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface UserService {
    Mono<User> get(String userId);

    Flux<User> list(PageRequest pageRequest);

    /**
     * Count the total number of elements returned by the {@link PageRequest}
     * ignoring {@link fr.ght1pc9kc.juery.api.Pagination}
     *
     * @param pageRequest The page request with filters
     * @return The total elements count
     */
    Mono<Integer> count(PageRequest pageRequest);
}
