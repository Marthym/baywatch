package fr.ght1pc9kc.baywatch.security.infra.adapters;

import fr.ght1pc9kc.baywatch.security.api.model.AuthenticationRequest;
import fr.ght1pc9kc.baywatch.security.api.model.BaywatchAuthentication;
import fr.ght1pc9kc.baywatch.security.infra.model.BaywatchUserDetails;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static fr.ght1pc9kc.baywatch.tests.samples.UserSamples.OBIWAN;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

class AuthenticationManagerAdapterTest {

    private AuthenticationManagerAdapter tested;

    @BeforeEach
    @SuppressWarnings("deprecation")
    void setUp() {
        UserServiceAdapter mockUserService = mock(UserServiceAdapter.class);
        doReturn(Mono.just(new BaywatchUserDetails(OBIWAN))).when(mockUserService).findByUsername(anyString());
        tested = new AuthenticationManagerAdapter(mockUserService, NoOpPasswordEncoder.getInstance());
    }

    @Test
    void should_authenticate_by_authentication() {
        StepVerifier.create(tested.authenticate(new UsernamePasswordAuthenticationToken(OBIWAN, "nawibo")))
                .assertNext(actual -> SoftAssertions.assertSoftly(soft -> {
                    soft.assertThat(actual).isInstanceOf(UsernamePasswordAuthenticationToken.class);
                    soft.assertThat(actual.getPrincipal()).isInstanceOf(BaywatchUserDetails.class);
                })).verifyComplete();
    }

    @Test
    void should_authenticate_by_request() {
        StepVerifier.create(tested.authenticate(new AuthenticationRequest(OBIWAN.self().login(), "nawibo", false)))
                .assertNext(actual -> SoftAssertions.assertSoftly(soft -> {
                    soft.assertThat(actual).isInstanceOf(BaywatchAuthentication.class);
                    soft.assertThat(actual.user()).isEqualTo(OBIWAN);
                })).verifyComplete();
    }
}