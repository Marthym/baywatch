package fr.ght1pc9kc.baywatch.admin.domain;

import fr.ght1pc9kc.baywatch.admin.api.StatisticsService;
import fr.ght1pc9kc.baywatch.admin.api.model.Counter;
import fr.ght1pc9kc.baywatch.admin.api.model.CounterGroup;
import fr.ght1pc9kc.baywatch.admin.api.model.CounterProvider;
import fr.ght1pc9kc.baywatch.common.api.exceptions.UnauthorizedException;
import fr.ght1pc9kc.baywatch.security.api.AuthenticationFacade;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@RequiredArgsConstructor
public class StatisticsServiceImpl implements StatisticsService {

    private final AuthenticationFacade authFacade;
    private final List<CounterProvider> counters;

    @Override
    public Flux<Counter> compute(CounterGroup group) {
        if (group == null) {
            return Flux.empty();
        }
        return authFacade.getConnectedUser()
                .switchIfEmpty(Mono.error(new UnauthorizedException()))
                .thenMany(Flux.fromIterable(counters))
                .filter(c -> c.group() == group)
                .flatMap(CounterProvider::computeCounter);
    }

    @Override
    public Flux<Counter> compute() {
        return authFacade.getConnectedUser()
                .switchIfEmpty(Mono.error(new UnauthorizedException()))
                .thenMany(Flux.fromIterable(counters))
                .flatMap(CounterProvider::computeCounter);
    }
}
