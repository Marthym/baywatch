package fr.ght1pc9kc.baywatch.api;

import fr.ght1pc9kc.baywatch.api.model.News;
import fr.ght1pc9kc.baywatch.api.model.search.Criteria;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collection;

public interface NewsPersistencePort {
    Mono<Void> persist(Collection<News> toCreate);

    Flux<News> list(Criteria searchCriteria);

    default Flux<News> list() {
        return list(Criteria.none());
    }

    Mono<Integer> delete(Collection<String> ids);
}
