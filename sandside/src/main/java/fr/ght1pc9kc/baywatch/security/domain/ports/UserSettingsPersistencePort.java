package fr.ght1pc9kc.baywatch.security.domain.ports;

import fr.ght1pc9kc.baywatch.security.api.model.UserSettings;
import fr.ght1pc9kc.entity.api.Entity;
import reactor.core.publisher.Mono;

public interface UserSettingsPersistencePort {
    Mono<Entity<UserSettings>> get(String id);

    Mono<Entity<UserSettings>> persist(String id, UserSettings userSettings);
}
