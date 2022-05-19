package fr.ght1pc9kc.baywatch.security.infra.adapters;

import fr.ght1pc9kc.baywatch.admin.api.model.Counter;
import fr.ght1pc9kc.baywatch.admin.api.model.CounterGroup;
import fr.ght1pc9kc.baywatch.admin.api.model.CounterProvider;
import fr.ght1pc9kc.baywatch.common.api.HeroIcons;
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
    public CounterGroup group() {
        return CounterGroup.SECURITY;
    }

    @Override
    public Mono<Counter> computeCounter() {
        return userRepository.count(QueryContext.empty())
                .map(r -> Counter.create(
                        "User Count",
                        HeroIcons.UserGroupIcon,
                        Integer.toString(r),
                        "Users in database")
                );
    }
}
