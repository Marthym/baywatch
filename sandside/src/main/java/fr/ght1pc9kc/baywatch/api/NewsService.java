package fr.ght1pc9kc.baywatch.api;

import fr.ght1pc9kc.baywatch.api.model.News;
import fr.ght1pc9kc.baywatch.api.model.RawNews;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface NewsService {
    Flux<RawNews> listSharedNews();

    Flux<News> listUserNews();

    Mono<RawNews> getSharedNews(String id);

    Mono<News> getUserNews(String id);
}
