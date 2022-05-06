package fr.ght1pc9kc.baywatch.techwatch.infra.adapters;

import fr.ght1pc9kc.baywatch.admin.api.model.Counter;
import fr.ght1pc9kc.baywatch.admin.api.model.CounterProvider;
import fr.ght1pc9kc.baywatch.admin.api.model.CounterType;
import fr.ght1pc9kc.baywatch.techwatch.api.model.News;
import fr.ght1pc9kc.baywatch.techwatch.domain.model.QueryContext;
import fr.ght1pc9kc.baywatch.techwatch.infra.persistence.NewsRepository;
import fr.ght1pc9kc.juery.api.Pagination;
import fr.ght1pc9kc.juery.api.pagination.Direction;
import fr.ght1pc9kc.juery.api.pagination.Sort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import static fr.ght1pc9kc.baywatch.common.api.model.EntitiesProperties.PUBLICATION;

@Component
@RequiredArgsConstructor
public class NewsCounterProvider implements CounterProvider {

    private final NewsRepository newsRepository;

    @Override
    public CounterType name() {
        return CounterType.NEWS_COUNT;
    }

    @Override
    public Mono<Counter> computeCounter() {
        Mono<News> lastNews = newsRepository.list(QueryContext.builder()
                .pagination(Pagination.of(0, 1, Sort.of(Direction.DESC, PUBLICATION)))
                .build()).next();
        Mono<Integer> count = newsRepository.count(QueryContext.empty());
        return Mono.zip(lastNews, count)
                .map(r -> new Counter("News Count", Integer.toString(r.getT2()), "last at " + r.getT1().getPublication()));
    }
}
