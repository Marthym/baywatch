package fr.ght1pc9kc.baywatch.security.domain;

import fr.ght1pc9kc.baywatch.security.api.UserSettingsService;
import fr.ght1pc9kc.baywatch.security.api.model.UserSettings;
import fr.ght1pc9kc.baywatch.security.domain.ports.UserSettingsPersistencePort;
import fr.ght1pc9kc.entity.api.Entity;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public class UserSettingsServiceImpl implements UserSettingsService {
    private final UserSettingsPersistencePort userServicePersistence;

    @Override
    public Mono<Entity<UserSettings>> get(String userId) {
        return userServicePersistence.get(userId);
    }

    @Override
    public Mono<Entity<UserSettings>> update(String userId, UserSettings userSettings) {
        return userServicePersistence.persist(userId, userSettings);
    }
}
