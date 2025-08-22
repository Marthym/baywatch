package fr.ght1pc9kc.baywatch.notify.infra.adapters;

import fr.ght1pc9kc.baywatch.notify.domain.exceptions.SmtpException;
import fr.ght1pc9kc.baywatch.notify.domain.model.Mail;
import fr.ght1pc9kc.baywatch.notify.domain.model.SmtpServerConfig;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.mail.internet.MimeMessage;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import reactor.core.scheduler.Schedulers;
import reactor.test.StepVerifier;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Duration;
import java.util.List;
import java.util.Properties;
import java.util.function.Supplier;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ReactiveSmtpMailSenderTest {

    private JavaMailSenderImpl mockJavaMailSender;
    private MimeMessage mockMimeMessage;
    private MeterRegistry mockMeterRegistry;
    private Counter mockCounter;
    private SmtpServerConfig deathStarConfig;

    private ReactiveSmtpMailSender tested;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {
        mockJavaMailSender = mock(JavaMailSenderImpl.class);
        mockMimeMessage = mock(MimeMessage.class);
        mockMeterRegistry = mock(MeterRegistry.class);
        mockCounter = mock(Counter.class);
        Supplier<JavaMailSenderImpl> mockJavaMailSenderFactory = (Supplier<JavaMailSenderImpl>) mock(Supplier.class);

        deathStarConfig = new SmtpServerConfig(
                "death-star.empire.galaxy",
                587,
                "vader@empire.galaxy",
                true,
                "vader",
                "force-strong-password",
                "TLSv1.3",
                true,
                true,
                Duration.ZERO
        );

        when(mockJavaMailSenderFactory.get()).thenReturn(mockJavaMailSender);
        when(mockJavaMailSender.getJavaMailProperties()).thenReturn(new Properties());
        when(mockMeterRegistry.counter(anyString(), any(String[].class))).thenReturn(mockCounter);
        when(mockJavaMailSender.createMimeMessage()).thenReturn(mockMimeMessage);

        tested = new ReactiveSmtpMailSender(
                "death-star-mailer",
                deathStarConfig,
                Schedulers.immediate(),
                mockJavaMailSenderFactory,
                mockMeterRegistry
        );
    }

    @Test
    void should_init_mail_sender_correctly() {
        verify(mockJavaMailSender).setHost("death-star.empire.galaxy");
        verify(mockJavaMailSender).setPort(587);
        verify(mockJavaMailSender).setUsername("vader");
        verify(mockJavaMailSender).setPassword("force-strong-password");

        verify(mockJavaMailSender).getJavaMailProperties();

        Properties actual = mockJavaMailSender.getJavaMailProperties();
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(actual.get("mail.smtp.auth")).isEqualTo("true");
            softly.assertThat(actual.get("mail.smtp.from")).isEqualTo("vader@empire.galaxy");
            softly.assertThat(actual.get("mail.smtp.starttls.enable")).isEqualTo("true");
            softly.assertThat(actual.get("mail.smtp.starttls.required")).isEqualTo("true");
            softly.assertThat(actual.get("mail.debug")).isEqualTo("false");
            softly.assertThat(actual.get("mail.smtp.ssl.checkserveridentity")).isEqualTo("true");
            softly.assertThat(actual.get("mail.smtp.ssl.protocols")).isEqualTo("TLSv1.3");
            softly.assertThat(actual.get("mail.smtp.connectiontimeout")).isEqualTo("10000");
            softly.assertThat(actual.get("mail.smtp.writetimeout")).isEqualTo("10000");
            softly.assertThat(actual.get("mail.smtp.timeout")).isEqualTo("10000");
        });
    }

    @Test
    void should_send_mail_successfully() {
        Mail empireMail = Mail.builder()
                .to("luke.skywalker@rebel.galaxy")
                .bcc(List.of("leia.organa@rebel.galaxy"))
                .subject("L'Empire vous propose une offre que vous ne pouvez refuser")
                .message("<h1>Rejoignez le côté obscur de la Force!</h1><p>Nous avons des cookies...</p>")
                .build();

        doNothing().when(mockJavaMailSender).send(any(MimeMessage.class));

        StepVerifier.create(tested.sendMail(empireMail))
                .assertNext(actual -> SoftAssertions.assertSoftly(softly -> {
                    softly.assertThat(actual.isFailure()).isFalse();
                    softly.assertThat(actual.get()).isNull();
                })).verifyComplete();

        verify(mockJavaMailSender).send(mockMimeMessage);
        verify(mockMeterRegistry).counter("rob.mailer.sendmail", "status", "200", "mailer", "death-star-mailer");
        verify(mockCounter).increment();
    }

    @Test
    void should_send_mail_with_attachment() throws Exception {
        Path deathStarPlans = Files.createTempFile("death-star-plans-", ".pdf");
        Files.write(deathStarPlans, "Plans secrets de l'Étoile de la Mort".getBytes(), StandardOpenOption.DELETE_ON_CLOSE);

        Path lightSaberSpecs = Files.createTempFile("saber-specifications-", ".txt");
        Files.write(lightSaberSpecs, "Spécifications du sabre laser rouge".getBytes(), StandardOpenOption.DELETE_ON_CLOSE);

        Mail mailWithAttachments = Mail.builder()
                .to("emperor@empire.galaxy")
                .subject("Plans secrets de l'Empire")
                .message("<p>Maître, voici les documents demandés.</p>")
                .attachment(deathStarPlans)
                .attachment(lightSaberSpecs)
                .build();

        doNothing().when(mockJavaMailSender).send(any(MimeMessage.class));

        StepVerifier.create(tested.sendMail(mailWithAttachments))
                .assertNext(actual -> SoftAssertions.assertSoftly(softly -> {
                    softly.assertThat(actual.isFailure()).isFalse();
                    softly.assertThat(actual.get()).isNull();
                })).verifyComplete();

        verify(mockJavaMailSender).send(mockMimeMessage);
        verify(mockCounter).increment();
    }

    @ParameterizedTest
    @CsvSource({
            "org.springframework.mail.MailAuthenticationException, 535",
            "org.springframework.mail.MailSendException, 450",
            "java.lang.RuntimeException, 421",
    })
    void should_fail_sending_mail_with_exception(Class<Throwable> exceptionClass, int expectedStatus) throws Exception {
        Mail rebellionMail = Mail.builder()
                .to("rebels@endor.galaxy")
                .subject("Les rebelles ont découvert notre position!")
                .message("Alerte rouge! Évacuation immédiate!")
                .build();

        Throwable exception = exceptionClass.getConstructor(String.class).newInstance("Fail sending email");
        doThrow(exception).when(mockJavaMailSender).send(any(MimeMessage.class));

        StepVerifier.create(tested.sendMail(rebellionMail))
                .assertNext(actual -> SoftAssertions.assertSoftly(softly -> {
                    softly.assertThat(actual.isFailure()).isTrue();
                    softly.assertThat(actual.getCause())
                            .isInstanceOf(SmtpException.class)
                            .hasMessage("Fail sending email")
                            .extracting(e -> ((SmtpException) e).getStatus())
                            .isEqualTo(expectedStatus);
                })).verifyComplete();

        verify(mockMeterRegistry).counter(
                "rob.mailer.sendmail", "status", Integer.toString(expectedStatus), "mailer", "death-star-mailer");
        verify(mockCounter).increment();
    }

    @Test
    void should_handle_exception_before_send_mail() {
        Mail forceDisturbance = Mail.builder()
                .to("yoda@dagobah.galaxy")
                .subject("Perturbation dans la Force détectée")
                .message("Une grande perturbance dans la Force, je ressens...")
                .build();

        when(mockJavaMailSender.createMimeMessage()).thenThrow(new IllegalStateException("Plus de mémoire dans l'holocron"));

        StepVerifier.create(tested.sendMail(forceDisturbance))
                .assertNext(actual -> SoftAssertions.assertSoftly(softly -> {
                    softly.assertThat(actual.isFailure()).isTrue();
                    softly.assertThat(actual.getCause())
                            .isInstanceOf(SmtpException.class)
                            .hasMessage("Plus de mémoire dans l'holocron")
                            .extracting(e -> ((SmtpException) e).getStatus())
                            .isEqualTo(500);
                })).verifyComplete();
    }

    @Test
    void should_use_default_constructor() {
        ReactiveSmtpMailSender defaultMailSender = new ReactiveSmtpMailSender(
                "tatooine-mailer",
                deathStarConfig,
                Schedulers.immediate(),
                mockMeterRegistry
        );

        Assertions.assertThat(defaultMailSender).isNotNull();
    }

    @Test
    void should_send_mail_without_bcc() {
        Mail emperorDirective = Mail.builder()
                .to("vader@empire.galaxy")
                .subject("Ordres directs de l'Empereur")
                .message("Exécutez l'Ordre 66 immédiatement!")
                .bcc(List.of())
                .build();

        doNothing().when(mockJavaMailSender).send(any(MimeMessage.class));

        StepVerifier.create(tested.sendMail(emperorDirective))
                .assertNext(tryResult -> Assertions.assertThat(tryResult.isFailure()).isFalse())
                .verifyComplete();

        verify(mockJavaMailSender).send(mockMimeMessage);
        verify(mockCounter).increment();
    }
}