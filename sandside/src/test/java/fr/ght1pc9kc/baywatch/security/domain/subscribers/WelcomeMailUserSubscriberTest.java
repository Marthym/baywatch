package fr.ght1pc9kc.baywatch.security.domain.subscribers;

import fr.ght1pc9kc.baywatch.common.api.model.TemplateVariable;
import fr.ght1pc9kc.baywatch.security.api.model.User;
import fr.ght1pc9kc.baywatch.security.domain.ports.KeyValuePersistencePort;
import fr.ght1pc9kc.baywatch.security.domain.ports.MailSenderPort;
import fr.ght1pc9kc.baywatch.security.domain.ports.UserEventPublisherPort;
import fr.ght1pc9kc.baywatch.tests.samples.UserSamples;
import fr.ght1pc9kc.entity.api.Entity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import reactor.core.Disposable;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.util.EnumMap;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class WelcomeMailUserSubscriberTest {

    private UserEventPublisherPort mockUserPublisher;
    private MailSenderPort mockMailSender;
    private KeyValuePersistencePort mockKeyValuePersistencePort;
    private Disposable mockDisposable;

    @BeforeEach
    void setUp() {
        mockUserPublisher = mock(UserEventPublisherPort.class);
        mockMailSender = mock(MailSenderPort.class);
        mockKeyValuePersistencePort = mock(KeyValuePersistencePort.class);
        mockDisposable = mock(Disposable.class);

        when(mockUserPublisher.onEvent(any())).thenReturn(mockDisposable);
        when(mockMailSender.send(any(), any(), any())).thenReturn(Mono.empty());
    }

    @Test
    @SuppressWarnings("unchecked")
    void should_subscribe_and_send_welcome_mail_on_new_user_event() {
        WelcomeMailUserSubscriber tested = new WelcomeMailUserSubscriber(
                mockUserPublisher, mockMailSender, mockKeyValuePersistencePort
        );

        ArgumentCaptor<Function<Entity<User>, Mono<Void>>> mapperCaptor =
                ArgumentCaptor.forClass(Function.class);
        verify(mockUserPublisher).onEvent(mapperCaptor.capture());

        StepVerifier.create(mapperCaptor.getValue().apply(UserSamples.LUKE))
                .verifyComplete();

        ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Entity<User>> userCaptor =
                ArgumentCaptor.forClass(Entity.class);
        ArgumentCaptor<Duration> ttlCaptor = ArgumentCaptor.forClass(Duration.class);
        verify(mockKeyValuePersistencePort).store(keyCaptor.capture(), userCaptor.capture(), ttlCaptor.capture());

        assertThat(keyCaptor.getValue())
                .startsWith(KeyValuePersistencePort.RESET_PASSWORD_PREFIX)
                .doesNotContain("=")
                .doesNotContain("+")
                .doesNotContain("/");
        assertThat(userCaptor.getValue()).isSameAs(UserSamples.LUKE);
        assertThat(ttlCaptor.getValue()).isEqualTo(Duration.ofMinutes(15));

        ArgumentCaptor<EnumMap<TemplateVariable, String>> variablesCaptor = ArgumentCaptor.forClass(EnumMap.class);
        verify(mockMailSender).send(
                eq(MailSenderPort.MailTemplateType.WELCOME_USER),
                eq(UserSamples.LUKE.self().mail()),
                variablesCaptor.capture()
        );

        EnumMap<TemplateVariable, String> variables = variablesCaptor.getValue();
        assertThat(variables).containsEntry(TemplateVariable.USERNAME, UserSamples.LUKE.self().login());
        assertThat(variables.get(TemplateVariable.TOKEN))
                .isNotBlank()
                .doesNotContain("=")
                .doesNotContain("+")
                .doesNotContain("/");

        tested.destroy();
    }

    @Test
    void should_dispose_subscription_on_destroy() {
        WelcomeMailUserSubscriber tested = new WelcomeMailUserSubscriber(
                mockUserPublisher, mockMailSender, mockKeyValuePersistencePort
        );

        tested.destroy();

        verify(mockDisposable, times(1)).dispose();
    }

    @Test
    @SuppressWarnings("unchecked")
    void should_propagate_mail_sender_error() {
        RuntimeException expected = new RuntimeException("smtp failure");
        when(mockMailSender.send(any(), any(), any())).thenReturn(Mono.error(expected));

        WelcomeMailUserSubscriber tested = new WelcomeMailUserSubscriber(
                mockUserPublisher, mockMailSender, mockKeyValuePersistencePort
        );

        ArgumentCaptor<Function<Entity<User>, Mono<Void>>> mapperCaptor =
                ArgumentCaptor.forClass(Function.class);
        verify(mockUserPublisher).onEvent(mapperCaptor.capture());

        StepVerifier.create(mapperCaptor.getValue().apply(UserSamples.OBIWAN))
                .verifyErrorSatisfies(actual -> assertThat(actual).isSameAs(expected));

        verify(mockKeyValuePersistencePort).store(any(), eq(UserSamples.OBIWAN), eq(Duration.ofMinutes(15)));
        tested.destroy();
    }
}
