package fr.ght1pc9kc.baywatch.security.infra.adapters;

import com.google.common.util.concurrent.AtomicDouble;
import fr.ght1pc9kc.baywatch.security.api.AuthenticationFacade;
import fr.ght1pc9kc.baywatch.security.infra.persistence.UserRepository;
import fr.ght1pc9kc.baywatch.techwatch.domain.model.QueryContext;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Component
@RequiredArgsConstructor
public class SecurityMetricsAdapter {
    private final MeterRegistry registry;
    private final UserRepository userRepository;

    private final AtomicDouble userCount = new AtomicDouble();

    @PostConstruct
    public void postConstruct() {
        Mono<Integer> newsCountEvent = userRepository.count(QueryContext.empty())
                .contextWrite(AuthenticationFacade.withSystemAuthentication())
                .cache(Duration.ofMinutes(10));

        Gauge.builder("bw.users.count", () -> {
                    newsCountEvent.subscribe(count -> userCount.set(count.doubleValue()));
                    return userCount.get();
                }).description("Total number of Users")
                .register(registry);
    }
}
