package fr.ght1pc9kc.baywatch.security.domain;

import fr.ght1pc9kc.baywatch.common.api.exceptions.UnauthorizedException;
import fr.ght1pc9kc.baywatch.security.api.AuthenticationFacade;
import fr.ght1pc9kc.baywatch.security.api.UserSettingsService;
import fr.ght1pc9kc.baywatch.security.api.model.NewsViewType;
import fr.ght1pc9kc.baywatch.security.api.model.UserSettings;
import fr.ght1pc9kc.baywatch.security.domain.exceptions.UnauthenticatedUser;
import fr.ght1pc9kc.baywatch.security.domain.model.AvailableLanguages;
import fr.ght1pc9kc.baywatch.security.domain.ports.ClientLocalePort;
import fr.ght1pc9kc.baywatch.security.domain.ports.UserSettingsPersistencePort;
import fr.ght1pc9kc.entity.api.Entity;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

import java.util.Comparator;
import java.util.Locale;

@RequiredArgsConstructor
public class UserSettingsServiceImpl implements UserSettingsService {
    private final UserSettingsPersistencePort userSettingsPersistence;
    private final AuthenticationFacade authenticationFacade;
    private final ClientLocalePort clientLocalePort;

    @Override
    public Mono<Entity<UserSettings>> get(String userId) {
        return authenticationFacade.getConnectedUser()
                .switchIfEmpty(Mono.error(() -> new UnauthenticatedUser("")))
                .filter(user -> user.id().equals(userId))
                .switchIfEmpty(Mono.error(() -> new UnauthorizedException("You can only request your own settings")))
                .flatMap(user -> userSettingsPersistence.get(user.id()))
                .switchIfEmpty(getDefaultUserSettings(userId));
    }

    @Override
    public Mono<Entity<UserSettings>> update(String userId, UserSettings userSettings) {
        return authenticationFacade.getConnectedUser()
                .switchIfEmpty(Mono.error(() -> new UnauthenticatedUser("")))
                .filter(user -> user.id().equals(userId))
                .switchIfEmpty(Mono.error(() -> new UnauthorizedException("You can only request your own settings")))
                .flatMap(user -> userSettingsPersistence.persist(user.id(), userSettings));
    }

    private Mono<Entity<UserSettings>> getDefaultUserSettings(String userId) {
        return clientLocalePort.getClientLocale().map(clientLocale -> {
            Locale locale = AvailableLanguages.ALL.stream()
                    .filter(l -> l.getLanguage().equals(clientLocale.getLanguage()))
                    .min(Comparator.comparing(Locale::getCountry, Comparator.nullsLast(Comparator.reverseOrder())))
                    .orElse(AvailableLanguages.DEFAULT);
            return Entity.identify(new UserSettings(locale, true, NewsViewType.MAGAZINE)).withId(userId);
        });
    }
}
