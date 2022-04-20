package fr.ght1pc9kc.baywatch.admin.domain;

import fr.ght1pc9kc.baywatch.admin.api.StatisticsService;
import fr.ght1pc9kc.baywatch.admin.api.model.Counter;
import fr.ght1pc9kc.baywatch.admin.api.model.CounterProvider;
import fr.ght1pc9kc.baywatch.admin.api.model.CounterType;
import fr.ght1pc9kc.baywatch.common.api.exceptions.UnauthorizedException;
import fr.ght1pc9kc.baywatch.security.api.AuthenticationFacade;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

import java.util.List;

@RequiredArgsConstructor
public class StatisticsServiceImpl implements StatisticsService {

    private final AuthenticationFacade authFacade;
    private final List<CounterProvider> counters;

    @Override
    public Mono<Counter> getNewsCount() {
        return computeCounterValue(CounterType.NEWS_COUNT);
    }

    @Override
    public Mono<Counter> getFeedsCount() {
        return computeCounterValue(CounterType.FEEDS_COUNT);
    }

    @Override
    public Mono<Counter> getUsersCount() {
        return computeCounterValue(CounterType.USERS_COUNT);
    }

    private Mono<Counter> computeCounterValue(CounterType type) {
        CounterProvider counter = counters.stream().filter(c -> c.name() == type).findFirst()
                .orElse(NoCounterProvider.NOP);
        return authFacade.getConnectedUser()
                .switchIfEmpty(Mono.error(new UnauthorizedException()))
                .flatMap(ignore -> counter.computeCounter());
    }
}
