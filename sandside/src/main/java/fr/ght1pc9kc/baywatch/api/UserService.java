package fr.ght1pc9kc.baywatch.api;

import fr.ght1pc9kc.baywatch.api.security.model.User;
import fr.ght1pc9kc.baywatch.api.model.request.PageRequest;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface UserService {
    Mono<User> get(String userId);

    Flux<User> list(PageRequest pageRequest);
}
