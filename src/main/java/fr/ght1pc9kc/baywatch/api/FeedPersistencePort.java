package fr.ght1pc9kc.baywatch.api;

import fr.ght1pc9kc.baywatch.api.model.Feed;
import reactor.core.publisher.Flux;

public interface FeedPersistencePort {
    Flux<Feed> list();
}
