package fr.ght1pc9kc.baywatch.security.domain;

import fr.ght1pc9kc.baywatch.common.api.exceptions.UnauthorizedException;
import fr.ght1pc9kc.baywatch.security.api.AuthenticationFacade;
import fr.ght1pc9kc.baywatch.security.api.UserSettingsService;
import fr.ght1pc9kc.baywatch.security.api.model.NewsViewType;
import fr.ght1pc9kc.baywatch.security.api.model.UserSettings;
import fr.ght1pc9kc.baywatch.security.domain.exceptions.UnauthenticatedUser;
import fr.ght1pc9kc.baywatch.security.domain.ports.UserSettingsPersistencePort;
import fr.ght1pc9kc.baywatch.tests.samples.UserSamples;
import fr.ght1pc9kc.entity.api.Entity;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Locale;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

class UserSettingsServiceImplTest {
    private UserSettingsService tested;
    private AuthenticationFacade mockAuthentication;

    @BeforeEach
    void setUp() {
        UserSettingsPersistencePort mockPersistence = mock(UserSettingsPersistencePort.class);
        doReturn(Mono.just(Entity.identify(new UserSettings(Locale.FRENCH, true, NewsViewType.MAGAZINE)).withId(UserSamples.LUKE.id())))
                .when(mockPersistence).get(anyString());

        doAnswer(answer -> {
            UserSettings settings = answer.getArgument(1, UserSettings.class);
            return Mono.just(Entity.identify(settings).withId(UserSamples.LUKE.id()));
        }).when(mockPersistence).persist(anyString(), any(UserSettings.class));

        mockAuthentication = mock(AuthenticationFacade.class);
        tested = new UserSettingsServiceImpl(mockPersistence, mockAuthentication);
    }

    @Test
    void should_get_user_settings() {
        doReturn(Mono.just(UserSamples.LUKE)).when(mockAuthentication).getConnectedUser();

        StepVerifier.create(tested.get(UserSamples.LUKE.id()))
                .assertNext(actual -> Assertions.assertThat(actual.id()).isEqualTo(UserSamples.LUKE.id()))
                .verifyComplete();
    }

    @Test
    void should_fail_get_settings_on_unauthorize() {
        doReturn(Mono.just(UserSamples.LUKE)).when(mockAuthentication).getConnectedUser();

        StepVerifier.create(tested.get(UserSamples.OBIWAN.id()))
                .verifyError(UnauthorizedException.class);
    }

    @Test
    void should_fail_get_settings_on_unauthenticated() {
        doReturn(Mono.empty()).when(mockAuthentication).getConnectedUser();

        StepVerifier.create(tested.get(UserSamples.OBIWAN.id()))
                .verifyError(UnauthenticatedUser.class);
    }

    @Test
    void should_update_user_settings() {
        doReturn(Mono.just(UserSamples.LUKE)).when(mockAuthentication).getConnectedUser();

        StepVerifier.create(tested.update(UserSamples.LUKE.id(), new UserSettings(Locale.FRENCH, true, NewsViewType.MAGAZINE)))
                .assertNext(actual -> Assertions.assertThat(actual.id()).isEqualTo(UserSamples.LUKE.id()))
                .verifyComplete();
    }

    @Test
    void should_fail_update_settings_on_unauthorize() {
        doReturn(Mono.just(UserSamples.LUKE)).when(mockAuthentication).getConnectedUser();

        StepVerifier.create(tested.update(UserSamples.OBIWAN.id(), new UserSettings(Locale.FRENCH, true, NewsViewType.MAGAZINE)))
                .verifyError(UnauthorizedException.class);
    }

    @Test
    void should_fail_update_settings_on_unauthenticated() {
        doReturn(Mono.empty()).when(mockAuthentication).getConnectedUser();

        StepVerifier.create(tested.update(UserSamples.OBIWAN.id(), new UserSettings(Locale.FRENCH, true, NewsViewType.MAGAZINE)))
                .verifyError(UnauthenticatedUser.class);
    }
}