package fr.ght1pc9kc.baywatch.security.infra.adapters;

import fr.ght1pc9kc.baywatch.tests.samples.UserSamples;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;

class UserEventPublisherAdapterTest {

    @Test
    void should_emit_published_user() {
        UserEventPublisherAdapter adapter = new UserEventPublisherAdapter();

        StepVerifier.create(adapter.events().take(1))
                .then(() -> adapter.publish(UserSamples.LUKE).block())
                .assertNext(user -> assertThat(user).isSameAs(UserSamples.LUKE))
                .verifyComplete();
    }
}
