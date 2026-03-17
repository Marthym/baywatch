package fr.ght1pc9kc.baywatch.security.infra.adapters;

import fr.ght1pc9kc.baywatch.common.api.model.ClientInfoContext;
import fr.ght1pc9kc.baywatch.security.api.AuthenticationFacade;
import fr.ght1pc9kc.baywatch.tests.samples.UserSamples;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import reactor.core.Disposable;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.net.InetSocketAddress;
import java.net.URI;
import java.util.NoSuchElementException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

class UserEventPublisherAdapterTest {
    private AuthenticationFacade mockAuthFacade;

    @BeforeEach
    void setUp() {
        mockAuthFacade = Mockito.mock(AuthenticationFacade.class);
    }

    @Test
    void should_emit_published_user() {
        when(mockAuthFacade.getClientInfoContext()).thenReturn(Mono.just(new ClientInfoContext(
                InetSocketAddress.createUnresolved("127.0.0.1", 0),
                "Baywatch", URI.create("https://localhost:8080/")
        )));
        UserEventPublisherAdapter tested = new UserEventPublisherAdapter(mockAuthFacade);

        Disposable disposable = tested.onEvent(u -> Mono.empty().then());
        Assertions.assertThat(disposable).isNotNull();

        StepVerifier.create(tested.publish(UserSamples.LUKE))
                .assertNext(user -> assertThat(user).isSameAs(UserSamples.LUKE))
                .verifyComplete();
    }

    @Test
    void should_emit_published_user_with_empty_context() {
        when(mockAuthFacade.getClientInfoContext()).thenReturn(Mono.empty());
        UserEventPublisherAdapter tested = new UserEventPublisherAdapter(mockAuthFacade);

        Disposable disposable = tested.onEvent(u -> Mono.empty().then());
        Assertions.assertThat(disposable).isNotNull();

        StepVerifier.create(tested.publish(UserSamples.LUKE))
                .verifyError(NoSuchElementException.class);
    }
}
