package fr.ght1pc9kc.baywatch.api;

import fr.ght1pc9kc.baywatch.api.model.News;
import fr.ght1pc9kc.baywatch.api.model.RawNews;
import fr.ght1pc9kc.baywatch.api.model.State;
import fr.ght1pc9kc.baywatch.api.model.search.Criteria;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collection;
import java.util.Map.Entry;

public interface NewsPersistencePort {
    Mono<Void> persist(Collection<News> toCreate);

    Flux<News> userList(Criteria searchCriteria);

    Flux<RawNews> list(Criteria searchCriteria);

    default Flux<News> userList() {
        return userList(Criteria.none());
    }

    Flux<Entry<String, State>> listState(Criteria searchCriteria);

    Mono<Integer> delete(Collection<String> ids);
}
