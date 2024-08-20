package fr.ght1pc9kc.baywatch.security.domain;

import fr.ght1pc9kc.baywatch.common.api.exceptions.UnauthorizedException;
import fr.ght1pc9kc.baywatch.security.api.AuthenticationFacade;
import fr.ght1pc9kc.baywatch.security.api.UserSettingsService;
import fr.ght1pc9kc.baywatch.security.api.model.UserSettings;
import fr.ght1pc9kc.baywatch.security.domain.exceptions.UnauthenticatedUser;
import fr.ght1pc9kc.baywatch.security.domain.ports.UserSettingsPersistencePort;
import fr.ght1pc9kc.entity.api.Entity;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public class UserSettingsServiceImpl implements UserSettingsService {
    private final UserSettingsPersistencePort userServicePersistence;
    private final AuthenticationFacade authenticationFacade;

    @Override
    public Mono<Entity<UserSettings>> get(String userId) {
        return authenticationFacade.getConnectedUser()
                .switchIfEmpty(Mono.error(() -> new UnauthenticatedUser("")))
                .filter(user -> user.id().equals(userId))
                .switchIfEmpty(Mono.error(() -> new UnauthorizedException("You can only request your own settings")))
                .flatMap(user -> userServicePersistence.get(user.id()));
    }

    @Override
    public Mono<Entity<UserSettings>> update(String userId, UserSettings userSettings) {
        return authenticationFacade.getConnectedUser()
                .switchIfEmpty(Mono.error(() -> new UnauthenticatedUser("")))
                .filter(user -> user.id().equals(userId))
                .switchIfEmpty(Mono.error(() -> new UnauthorizedException("You can only request your own settings")))
                .flatMap(user -> userServicePersistence.persist(user.id(), userSettings));
    }
}
