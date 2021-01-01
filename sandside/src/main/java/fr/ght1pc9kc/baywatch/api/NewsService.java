package fr.ght1pc9kc.baywatch.api;

import fr.ght1pc9kc.baywatch.api.model.News;
import fr.ght1pc9kc.baywatch.api.model.request.PageRequest;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface NewsService {
    /**
     * List {@link News} for connected user or {@link fr.ght1pc9kc.baywatch.api.model.RawNews} for anonymous.
     * For Anonymous, {@link fr.ght1pc9kc.baywatch.api.model.State} is always
     * {@link fr.ght1pc9kc.baywatch.api.model.State#NONE}
     *
     * @param pageRequest {@see PageRequest}
     * @return The {@link News} for connected user or {@link fr.ght1pc9kc.baywatch.api.model.RawNews} for anonymous
     */
    Flux<News> list(PageRequest pageRequest);

    Mono<News> get(String id);
}
