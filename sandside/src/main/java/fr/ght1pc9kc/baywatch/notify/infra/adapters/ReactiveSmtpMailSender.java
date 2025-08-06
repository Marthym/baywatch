package fr.ght1pc9kc.baywatch.notify.infra.adapters;

import fr.ght1pc9kc.baywatch.common.domain.Try;
import fr.ght1pc9kc.baywatch.notify.domain.model.Mail;
import fr.ght1pc9kc.baywatch.notify.domain.model.SmtpException;
import fr.ght1pc9kc.baywatch.notify.domain.model.SmtpServerConfig;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.MailAuthenticationException;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Properties;
import java.util.function.Supplier;

public class ReactiveSmtpMailSender {
    private static final int SMTP_ERROR_SERVICE_UNAVAILABLE = 421;
    private static final int SMTP_ERROR_MAILBOX_UNAVAILABLE = 450;
    private static final int SMTP_ERROR_SYNTAX_ERROR = 500;
    private static final int SMTP_ERROR_INVALID_CREDENTIALS = 535;

    private static final String COUNTER_SEND_MAIL = "rob.mailer.sendmail";
    private static final String COUNTER_TAG_STATUS = "status";
    private static final String COUNTER_TAG_MAILER = "mailer";

    private final String id;
    private final JavaMailSenderImpl mailSender;
    private final Scheduler mailScheduler;
    private final MeterRegistry meterRegistry;

    public ReactiveSmtpMailSender(String id, SmtpServerConfig config, String defaultFrom, Scheduler mailScheduler, MeterRegistry meterRegistry) {
        this(id, config, defaultFrom, mailScheduler, JavaMailSenderImpl::new, meterRegistry);
    }

    public ReactiveSmtpMailSender(String id, SmtpServerConfig config, String defaultFrom, Scheduler mailScheduler,
                                  Supplier<JavaMailSenderImpl> javaMailSenderFactory, MeterRegistry meterRegistry) {
        this.id = id;
        this.mailScheduler = mailScheduler;
        this.mailSender = javaMailSenderFactory.get();
        this.mailSender.setHost(config.server());
        this.mailSender.setPort(config.port());

        this.mailSender.setUsername(config.username());
        this.mailSender.setPassword(config.password());

        this.meterRegistry = meterRegistry;

        Properties props = this.mailSender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", config.secure());
        props.put("mail.smtp.from", defaultFrom);
        props.put("mail.smtp.starttls.enable", config.requireTls());
        props.put("mail.smtp.starttls.required", config.requireTls());
        props.put("mail.debug", "false");
    }

    /**
     * Send an email through SMTP protocol
     *
     * @param mail The mail to send
     * @return {@code Mono<Try<Void>>} when the sending was completed.
     * @throws SmtpException on unknown exception with status -1
     *                       The {@code Try} can contain failure as {@link SmtpException} with status code :
     *                       <ul>
     *                           <li><strong>421</strong>: Service not available, closing transmission channel, can be retried</li>
     *                           <li><strong>450</strong>: Requested mail action not taken: mailbox unavailable, can be retried</li>
     *                           <li><strong>500</strong>: Syntax error, command unrecognized, mail address or message is malformed</li>
     *                           <li><strong>535</strong>: Authentication credentials invalid, can't be recovered</li>
     *                       </ul>
     */
    public Mono<Try<Void>> sendMail(Mail mail) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, !mail.attachments().isEmpty(), StandardCharsets.UTF_8.name());

            helper.setTo(mail.to());
            helper.setSubject(mail.subject());
            helper.setText(mail.message(), true);
            for (String address : mail.bcc()) {
                helper.addBcc(address);
            }

            for (Path attachment : mail.attachments()) {
                FileSystemResource resource = new FileSystemResource(attachment);
                helper.addAttachment(attachment.getFileName().toString(), resource);
            }
            return Mono.<Try<Void>>create(sink -> {
                try {
                    mailSender.send(message);
                    meterRegistry.counter(COUNTER_SEND_MAIL,
                            COUNTER_TAG_STATUS, "200",
                            COUNTER_TAG_MAILER, id).increment();
                    sink.success(Try.success(null));
                } catch (MailAuthenticationException e) {
                    sink.success(Try.fail(new SmtpException(SMTP_ERROR_INVALID_CREDENTIALS, e.getLocalizedMessage())));
                    meterRegistry.counter(COUNTER_SEND_MAIL,
                            COUNTER_TAG_STATUS, String.valueOf(SMTP_ERROR_INVALID_CREDENTIALS),
                            COUNTER_TAG_MAILER, id).increment();
                } catch (MailSendException e) {
                    sink.success(Try.fail(new SmtpException(SMTP_ERROR_MAILBOX_UNAVAILABLE, e.getLocalizedMessage())));
                    meterRegistry.counter(COUNTER_SEND_MAIL,
                            COUNTER_TAG_STATUS, String.valueOf(SMTP_ERROR_MAILBOX_UNAVAILABLE),
                            COUNTER_TAG_MAILER, id).increment();
                } catch (Exception e) {
                    sink.success(Try.fail(new SmtpException(SMTP_ERROR_SERVICE_UNAVAILABLE, e.getLocalizedMessage())));
                    meterRegistry.counter(COUNTER_SEND_MAIL,
                            COUNTER_TAG_STATUS, String.valueOf(SMTP_ERROR_SERVICE_UNAVAILABLE),
                            COUNTER_TAG_MAILER, id).increment();
                }
            }).subscribeOn(mailScheduler);
        } catch (MessagingException e) {
            return Mono.just(Try.fail(new SmtpException(SMTP_ERROR_SYNTAX_ERROR, e.getLocalizedMessage())));
        } catch (Exception e) {
            return Mono.error(new SmtpException(e));
        }
    }
}
