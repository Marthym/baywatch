package fr.ght1pc9kc.baywatch.notify.infra.adapters;

import fr.ght1pc9kc.baywatch.admin.api.model.Counter;
import fr.ght1pc9kc.baywatch.admin.api.model.CounterGroup;
import fr.ght1pc9kc.baywatch.admin.api.model.CounterProvider;
import fr.ght1pc9kc.baywatch.common.api.model.HeroIcons;
import fr.ght1pc9kc.baywatch.notify.api.NotifyManager;
import fr.ght1pc9kc.baywatch.notify.api.NotifyService;
import fr.ght1pc9kc.baywatch.notify.domain.NotifyServiceImpl;
import fr.ght1pc9kc.baywatch.notify.domain.ports.NotificationPersistencePort;
import fr.ght1pc9kc.baywatch.security.api.AuthenticationFacade;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.annotation.PreDestroy;
import lombok.experimental.Delegate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Clock;

@Slf4j
@Service
public class NotifyServiceAdapter implements NotifyService, NotifyManager, CounterProvider {
    @Delegate
    private final NotifyServiceImpl delegate;

    public NotifyServiceAdapter(
            AuthenticationFacade authFacade, NotificationPersistencePort notificationPersistence, MeterRegistry registry) {
        this.delegate = new NotifyServiceImpl(authFacade, notificationPersistence, Clock.systemUTC());

        Gauge.builder("bw.session_count", delegate::countCacheEntries)
                .baseUnit("gauge")
                .description("Active Sessions")
                .register(registry);
    }

    @PreDestroy
    private void preDestroy() {
        delegate.close();
        log.info("All Notifications Session complete gracefully.");
    }

    @Override
    public CounterGroup group() {
        return CounterGroup.SYSTEM;
    }

    @Override
    public Mono<Counter> computeCounter() {
        return Mono.fromCallable(delegate::countCacheEntries)
                .map(count -> Counter.create("Session Count", HeroIcons.USERS_ICON, Long.toString(count), ""));
    }
}
