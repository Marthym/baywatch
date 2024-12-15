package fr.ght1pc9kc.baywatch.techwatch.domain.ports;

import fr.ght1pc9kc.baywatch.techwatch.api.model.News;
import fr.ght1pc9kc.baywatch.common.domain.QueryContext;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collection;

public interface NewsPersistencePort {
    Mono<News> get(QueryContext qCtx);

    default Flux<News> list() {
        return list(QueryContext.empty());
    }

    Flux<News> list(QueryContext qCtx);

    default Flux<String> listId(QueryContext qCtx) {
        return list().map(News::id);
    }

    Mono<Integer> persist(Collection<News> toCreate);

    Mono<Integer> delete(Collection<String> ids);

    Mono<Integer> count(QueryContext qCtx);
}
