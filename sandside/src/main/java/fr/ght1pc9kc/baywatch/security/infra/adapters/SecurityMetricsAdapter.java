package fr.ght1pc9kc.baywatch.security.infra.adapters;

import fr.ght1pc9kc.baywatch.common.domain.QueryContext;
import fr.ght1pc9kc.baywatch.security.api.AuthenticationFacade;
import fr.ght1pc9kc.baywatch.security.infra.persistence.UserRepository;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;

@Component
@RequiredArgsConstructor
public class SecurityMetricsAdapter {
    private final MeterRegistry registry;
    private final UserRepository userRepository;

    private final AtomicInteger userCount = new AtomicInteger();

    @PostConstruct
    public void postConstruct() {
        Mono<Integer> newsCountEvent = userRepository.count(QueryContext.empty())
                .contextWrite(AuthenticationFacade.withSystemAuthentication())
                .cache(Duration.ofMinutes(10));

        Gauge.builder("bw.users.count", () -> {
                    newsCountEvent.subscribe(userCount::set);
                    return userCount.get();
                }).description("Total number of Users")
                .register(registry);
    }
}
