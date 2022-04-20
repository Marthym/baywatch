package fr.ght1pc9kc.baywatch.techwatch.infra.adapters;

import fr.ght1pc9kc.baywatch.admin.api.model.Counter;
import fr.ght1pc9kc.baywatch.admin.api.model.CounterProvider;
import fr.ght1pc9kc.baywatch.admin.api.model.CounterType;
import fr.ght1pc9kc.baywatch.techwatch.domain.model.QueryContext;
import fr.ght1pc9kc.baywatch.techwatch.infra.persistence.FeedRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class FeedsCounterProvider implements CounterProvider {

    private final FeedRepository feedRepository;

    @Override
    public CounterType name() {
        return CounterType.FEEDS_COUNT;
    }

    @Override
    public Mono<Counter> computeCounter() {
        return feedRepository.count(QueryContext.empty())
                .map(r -> new Counter("Feed Count", Integer.toString(r), "total feeds in database"));
    }
}
