package fr.ght1pc9kc.baywatch.techwatch.infra.adapters;

import fr.ght1pc9kc.baywatch.admin.api.model.CounterProvider;
import fr.ght1pc9kc.baywatch.admin.api.model.CounterType;
import fr.ght1pc9kc.baywatch.techwatch.domain.model.QueryContext;
import fr.ght1pc9kc.baywatch.techwatch.infra.persistence.NewsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
public class NewsCounterProvider implements CounterProvider {

    private final NewsRepository newsRepository;

    @Override
    public CounterType name() {
        return CounterType.NEWS_COUNT;
    }

    @Override
    public Mono<BigDecimal> computeNumeric() {
        return newsRepository.count(QueryContext.empty())
                .map(BigDecimal::new);
    }
}
