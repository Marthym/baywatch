package fr.ght1pc9kc.baywatch.security.infra.adapters;

import fr.ght1pc9kc.baywatch.admin.api.model.Counter;
import fr.ght1pc9kc.baywatch.admin.api.model.CounterProvider;
import fr.ght1pc9kc.baywatch.admin.api.model.CounterType;
import fr.ght1pc9kc.baywatch.security.infra.persistence.UserRepository;
import fr.ght1pc9kc.baywatch.techwatch.domain.model.QueryContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class UsersCounterProvider implements CounterProvider {

    private final UserRepository userRepository;

    @Override
    public CounterType name() {
        return CounterType.USERS_COUNT;
    }

    @Override
    public Mono<Counter> computeCounter() {
        return userRepository.count(QueryContext.empty())
                .map(r -> new Counter("User Count", Integer.toString(r), "Users in database"));
    }
}
