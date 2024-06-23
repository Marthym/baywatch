package fr.ght1pc9kc.baywatch.security.infra.adapters;

import fr.ght1pc9kc.baywatch.common.infra.filters.ReactiveClientInfoContextHolder;
import fr.ght1pc9kc.baywatch.tests.samples.UserSamples;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

import java.net.InetSocketAddress;

class SpringAuthenticationContextTest {

    private final SpringAuthenticationContext tested = new SpringAuthenticationContext();

    @Test
    void should_get_connected_user() {
        StepVerifier.create(tested.getConnectedUser()
                        .contextWrite(tested.withAuthentication(UserSamples.OBIWAN)))
                .assertNext(actual -> Assertions.assertThat(actual.id()).isEqualTo("US01GRQ11XKGHERDEBSCHBNJAY78"))
                .verifyComplete();
    }

    @Test
    void should_get_client_info_context() {
        StepVerifier.create(tested.getClientInfoContext()
                        .contextWrite(ReactiveClientInfoContextHolder.withClientInfo(
                                InetSocketAddress.createUnresolved("127.0.0.1", 80), "Dummy User Agent")))
                .assertNext(actual -> Assertions.assertThat(actual.ip().toString())
                        .isEqualToIgnoringWhitespace("127.0.0.1/<unresolved>:80"))
                .verifyComplete();
    }
}