package fr.ght1pc9kc.baywatch.security.api;

import fr.ght1pc9kc.baywatch.security.api.model.UserSettings;
import fr.ght1pc9kc.entity.api.Entity;
import reactor.core.publisher.Mono;

public interface UserSettingsService {
    Mono<Entity<UserSettings>> get(String userId);

    Mono<Entity<UserSettings>> update(String userId, UserSettings userSettings);
}
