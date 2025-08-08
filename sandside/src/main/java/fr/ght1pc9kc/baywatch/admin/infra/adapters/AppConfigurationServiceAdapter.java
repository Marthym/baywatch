package fr.ght1pc9kc.baywatch.admin.infra.adapters;

import fr.ght1pc9kc.baywatch.admin.api.AppConfigurationService;
import fr.ght1pc9kc.baywatch.admin.domain.ports.ConfigurationPersistencePort;
import fr.ght1pc9kc.baywatch.admin.domain.services.AppConfigurationServiceImpl;
import lombok.experimental.Delegate;
import org.springframework.stereotype.Service;

@Service
public class AppConfigurationServiceAdapter implements AppConfigurationService {
    @Delegate
    private final AppConfigurationService delegate;

    public AppConfigurationServiceAdapter(ConfigurationPersistencePort persistencePort) {
        this.delegate = new AppConfigurationServiceImpl(persistencePort);
    }
}
