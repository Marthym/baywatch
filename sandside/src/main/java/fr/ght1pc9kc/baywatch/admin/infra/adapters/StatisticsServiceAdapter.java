package fr.ght1pc9kc.baywatch.admin.infra.adapters;

import fr.ght1pc9kc.baywatch.admin.api.StatisticsService;
import fr.ght1pc9kc.baywatch.admin.api.model.CounterProvider;
import fr.ght1pc9kc.baywatch.admin.domain.StatisticsServiceImpl;
import fr.ght1pc9kc.baywatch.security.api.AuthenticationFacade;
import lombok.experimental.Delegate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StatisticsServiceAdapter implements StatisticsService {
    @Delegate
    private final StatisticsService delegate;

    public StatisticsServiceAdapter(AuthenticationFacade authFacade, List<CounterProvider> counters) {
        this.delegate = new StatisticsServiceImpl(authFacade, counters);
    }
}
