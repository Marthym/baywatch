package fr.ght1pc9kc.baywatch.security.domain;

import fr.ght1pc9kc.baywatch.common.api.LocaleFacade;
import fr.ght1pc9kc.baywatch.security.api.AuthenticationFacade;
import fr.ght1pc9kc.baywatch.security.api.PasswordService;
import fr.ght1pc9kc.baywatch.security.api.model.User;
import fr.ght1pc9kc.baywatch.security.infra.adapters.PasswordCheckerNbvcxz;
import fr.ght1pc9kc.baywatch.tests.samples.UserSamples;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Locale;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PasswordServiceImplTest {

    private PasswordService tested;

    @BeforeEach
    void setUp() {
        AuthenticationFacade authenticationFacade = mock(AuthenticationFacade.class);
        when(authenticationFacade.getConnectedUser()).thenReturn(Mono.just(UserSamples.OBIWAN));
        LocaleFacade localeFacade = mock(LocaleFacade.class);
        when(localeFacade.getLocale()).thenReturn(Mono.just(Locale.ENGLISH));

        tested = new PasswordServiceImpl(authenticationFacade, new PasswordCheckerNbvcxz(), localeFacade);
    }

    @ParameterizedTest
    @CsvSource({
            "123, false",
            "obiwan, false",
            "154uytfoKenobi123!, false",
            "J0Qs, false",
            "Ohvahoizaetho1at, true",
    })
    void should_check_password_strength_when_authenticated(String password, boolean expectedSecure) {
        StepVerifier.create(tested.checkPasswordStrength(password))
                .assertNext(actual -> Assertions.assertThat(actual.isSecure()).isEqualTo(expectedSecure))
                .verifyComplete();
    }

    @ParameterizedTest
    @CsvSource({
            "123, false",
            "luke, false",
            "154uytfluke123skywalker123!, false",
            "J0Qs, false",
            "Ohvahoizaetho1at, true",
    })
    void should_check_password_strength_for_anonymous(String password, boolean expectedSecure) {
        User user = UserSamples.LUKE.self().withPassword(password);
        StepVerifier.create(tested.checkPasswordStrength(user))
                .assertNext(actual -> Assertions.assertThat(actual.isSecure()).isEqualTo(expectedSecure))
                .verifyComplete();
    }

    @Test
    void should_generate_password() {
        StepVerifier.create(tested.generateSecurePassword(5))
                .assertNext(actual -> Assertions.assertThat(actual).isNotBlank().hasSize(16))
                .assertNext(actual -> Assertions.assertThat(actual).isNotBlank().hasSize(16))
                .assertNext(actual -> Assertions.assertThat(actual).isNotBlank().hasSize(16))
                .assertNext(actual -> Assertions.assertThat(actual).isNotBlank().hasSize(16))
                .assertNext(actual -> Assertions.assertThat(actual).isNotBlank().hasSize(16))
                .verifyComplete();
    }
}