package fr.ght1pc9kc.baywatch.admin.infra.adapters;

import fr.ght1pc9kc.baywatch.admin.api.model.CounterProvider;
import fr.ght1pc9kc.baywatch.admin.domain.UptimeCounterProvider;
import lombok.experimental.Delegate;
import org.springframework.stereotype.Component;

@Component
public class UptimeCounterAdapter implements CounterProvider {

    @Delegate
    private final UptimeCounterProvider delegate;

    public UptimeCounterAdapter() {
        delegate = new UptimeCounterProvider();
    }
}
