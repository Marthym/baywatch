package fr.ght1pc9kc.baywatch.notify.infra.controllers;

import fr.ght1pc9kc.baywatch.common.domain.Try;
import fr.ght1pc9kc.baywatch.notify.domain.model.Mail;
import fr.ght1pc9kc.baywatch.notify.domain.model.SmtpServerConfig;
import fr.ght1pc9kc.baywatch.notify.domain.ports.MailQueuePersistencePort;
import fr.ght1pc9kc.baywatch.notify.domain.ports.SmtpConfigurationPort;
import fr.ght1pc9kc.baywatch.notify.infra.adapters.ReactiveSmtpMailSender;
import fr.ght1pc9kc.entity.api.Entity;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class MailQueueSchedulerTest {

    private SmtpConfigurationPort smtpConfigurationPort;
    private MailQueuePersistencePort mailQueuePersistencePort;
    private ReactiveSmtpMailSender mockMailSender;

    private MailQueueScheduler tested;

    @BeforeEach
    void setUp() {
        smtpConfigurationPort = mock(SmtpConfigurationPort.class);
        mailQueuePersistencePort = mock(MailQueuePersistencePort.class);
        when(smtpConfigurationPort.get()).thenReturn(Mono.just(new SmtpServerConfig(
                "mail.jedi.com", 587, "yoda@jedi.com", true,
                "obiwan", "kenobi", "TLSv1.3", true, true, Duration.ZERO)));
        when(mailQueuePersistencePort.consume()).thenReturn(Flux.just(
                Entity.identify(Mail.builder()
                        .to("darth.vader@sith.com")
                        .subject("Where is may dad")
                        .message("I'm looking for my dad; have you see it ?")
                        .build()).withId("ML01K2HTJW8ZV4TMKJKSFVHQWY0C"),
                Entity.identify(Mail.builder()
                        .to("yoda@sith.com")
                        .subject("May the fourth")
                        .message("Hello, happy Force Day master")
                        .build()).withId("ML01K2HTJWMZXY8PZ5QB0228VGF1")
        ));


        MeterRegistry meterRegistry = new SimpleMeterRegistry();
        tested = spy(new MailQueueScheduler(
                smtpConfigurationPort,
                meterRegistry,
                mailQueuePersistencePort
        ));

        mockMailSender = mock(ReactiveSmtpMailSender.class);
        when(mockMailSender.sendMail(any(Mail.class))).thenReturn(Mono.just(Try.of(() -> null)));
        doReturn(mockMailSender).when(tested).newReactiveSmtpMailSender(any(SmtpServerConfig.class));
    }

    @Test
    void should_run_mail_queue() {
        tested.run();

        verify(smtpConfigurationPort).get();
        verify(mailQueuePersistencePort).consume();
        verify(mockMailSender, times(2)).sendMail(any(Mail.class));
    }

    @Test
    void should_handle_smtp_error() {
        when(mockMailSender.sendMail(any(Mail.class))).thenReturn(Mono.just(Try.fail(new RuntimeException("Bad mail sender"))));
        tested.run();

        verify(smtpConfigurationPort).get();
        verify(mailQueuePersistencePort).consume();
        verify(mockMailSender, times(2)).sendMail(any(Mail.class));
    }

    @Test
    void should_handle_mail_sender_error() {
        when(mockMailSender.sendMail(any(Mail.class))).thenReturn(Mono.error(new RuntimeException("Bad mail sender")));
        tested.run();

        verify(smtpConfigurationPort).get();
        verify(mailQueuePersistencePort).consume();
        verify(mockMailSender).sendMail(any(Mail.class));
    }

    @Test
    void should_start_mail_queue() {
        tested.startMailQueue();

        Assertions.assertThatNoException().isThrownBy(() -> tested.shutdownMailQueue());
    }

    @AfterEach
    void tearDown() {
        tested.shutdownMailQueue();
    }
}