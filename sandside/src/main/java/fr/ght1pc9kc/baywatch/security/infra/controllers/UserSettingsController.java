package fr.ght1pc9kc.baywatch.security.infra.controllers;

import fr.ght1pc9kc.baywatch.security.api.UserSettingsService;
import fr.ght1pc9kc.baywatch.security.api.model.UserSettings;
import fr.ght1pc9kc.baywatch.security.infra.mappers.UserSettingsMapper;
import fr.ght1pc9kc.baywatch.security.infra.model.UserSettingsForm;
import fr.ght1pc9kc.entity.api.Entity;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Mono;

@Controller
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class UserSettingsController {

    private final UserSettingsService userSettingsService;
    private final UserSettingsMapper userSettingsMapper;

    @QueryMapping
    public Mono<Entity<UserSettings>> userSettingsGet(@Argument("userId") String userId) {
        return userSettingsService.get(userId);
    }

    @MutationMapping
    public Mono<Entity<UserSettings>> userSettingsUpdate(
            @Argument("userId") String userId, @Argument("settings") UserSettingsForm settings) {
        return userSettingsService.update(userId, userSettingsMapper.get(settings));
    }
}
