package fr.ght1pc9kc.baywatch.techwatch.infra.adapters;

import fr.ght1pc9kc.baywatch.security.api.AuthenticationFacade;
import fr.ght1pc9kc.baywatch.techwatch.api.SystemMaintenanceService;
import fr.ght1pc9kc.baywatch.techwatch.domain.SystemMaintenanceServiceImpl;
import fr.ght1pc9kc.baywatch.techwatch.domain.ports.FeedPersistencePort;
import fr.ght1pc9kc.baywatch.techwatch.domain.ports.NewsPersistencePort;
import lombok.experimental.Delegate;
import org.springframework.stereotype.Service;

@Service
public class SystemMaintenanceAdapter implements SystemMaintenanceService {
    @Delegate
    private final SystemMaintenanceService delegate;

    public SystemMaintenanceAdapter(FeedPersistencePort feedPersistence, NewsPersistencePort newsPersistence, AuthenticationFacade authentFacade) {
        this.delegate = new SystemMaintenanceServiceImpl(feedPersistence, newsPersistence, authentFacade);
    }
}
