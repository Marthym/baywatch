package fr.ght1pc9kc.baywatch.security.infra.adapters;

import fr.ght1pc9kc.baywatch.security.api.AuthenticationFacade;
import fr.ght1pc9kc.baywatch.security.api.UserSettingsService;
import fr.ght1pc9kc.baywatch.security.domain.UserSettingsServiceImpl;
import fr.ght1pc9kc.baywatch.security.domain.ports.UserSettingsPersistencePort;
import lombok.experimental.Delegate;
import org.springframework.stereotype.Service;

@Service
public class UserSettingsServiceAdapter implements UserSettingsService {
    @Delegate
    private final UserSettingsService delegate;

    public UserSettingsServiceAdapter(AuthenticationFacade authenticationFacade, UserSettingsPersistencePort persistencePort) {
        this.delegate = new UserSettingsServiceImpl(persistencePort, authenticationFacade);
    }
}
