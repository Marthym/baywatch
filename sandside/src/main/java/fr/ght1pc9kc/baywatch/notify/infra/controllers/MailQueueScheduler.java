package fr.ght1pc9kc.baywatch.notify.infra.controllers;

import fr.ght1pc9kc.baywatch.notify.domain.exceptions.NotifyModuleException;
import fr.ght1pc9kc.baywatch.notify.domain.model.SmtpServerConfig;
import fr.ght1pc9kc.baywatch.notify.domain.ports.MailQueuePersistencePort;
import fr.ght1pc9kc.baywatch.notify.domain.ports.SmtpConfigurationPort;
import fr.ght1pc9kc.baywatch.notify.infra.adapters.ReactiveSmtpMailSender;
import graphql.VisibleForTesting;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class MailQueueScheduler implements Runnable {
    private static final String STACKTRACE = "STACKTRACE";
    private final ScheduledExecutorService scheduleExecutor = Executors.newSingleThreadScheduledExecutor(
            new CustomizableThreadFactory("mailQueueScheduler-"));
    private final Scheduler mqScheduler = Schedulers.newBoundedElastic(
            4, Integer.MAX_VALUE, "mailQueue-", 10, true);

    private final SmtpConfigurationPort smtpConfigurationPort;
    private final MeterRegistry meterRegistry;
    private final MailQueuePersistencePort mailQueuePersistencePort;

    @Async
    @EventListener(ApplicationStartedEvent.class)
    public void startMailQueue() {
        log.atDebug().log("Start mail queue in 2 minutes ...");
        scheduleExecutor.schedule(this, smtpConfigurationPort.getMailQueuePollingInterval().toSeconds(), TimeUnit.SECONDS);
    }

    @PreDestroy
    @SneakyThrows
    public void shutdownMailQueue() {
        log.atInfo().log("Commencing graceful shutdown. ⏳ Waiting for mail queue to empty");
        scheduleExecutor.shutdown();
        if (!scheduleExecutor.awaitTermination(5, TimeUnit.MINUTES)) {
            log.atWarn().log("Mail queue scheduler shutdown timeout ! Some mails can be lost \uD83D\uDE31 !");
        }
        mqScheduler.dispose();
        log.atInfo().log("Graceful shutdown complete");
    }

    @Override
    public void run() {
        log.atTrace().log("Start mail queue consumer ...");
        try {
            CountDownLatch latch = new CountDownLatch(1);
            smtpConfigurationPort.get()
                    .switchIfEmpty(Mono.error(() -> new IllegalStateException("No SMTP configuration found")))
                    .map(this::newReactiveSmtpMailSender)
                    .flatMapMany(sender -> mailQueuePersistencePort.consume()
                            .flatMap(mail -> sender.sendMail(mail.self())))
                    .subscribe(smtpResult -> {
                                if (smtpResult.isFailure()) {
                                    log.atError()
                                            .addArgument(smtpResult.getCause().getClass())
                                            .addArgument(smtpResult.getCause().getLocalizedMessage())
                                            .log("Error sending mail: {}: {}");
                                    log.atDebug().log(STACKTRACE, smtpResult.getCause());
                                }
                            }, error -> {
                                log.atError()
                                        .addArgument(error.getClass())
                                        .addArgument(error.getLocalizedMessage())
                                        .log("Mail queue stop with error -> {}: {}");
                                log.atDebug().log(STACKTRACE, error);
                                latch.countDown();
                            },
                            () -> {
                                log.atDebug().log("Mail queue complete");
                                if (!scheduleExecutor.isShutdown()) {
                                    scheduleExecutor.schedule(this,
                                            smtpConfigurationPort.getMailQueuePollingInterval().toSeconds(), TimeUnit.SECONDS);
                                }
                                latch.countDown();
                            });
            if (!latch.await(2, TimeUnit.MINUTES)) {
                log.atWarn().log("Mail queue scheduler latch timeout ! Operation can be blocked ?");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new NotifyModuleException("MailQueue thread interrupted !", e);
        }
    }

    @VisibleForTesting
    ReactiveSmtpMailSender newReactiveSmtpMailSender(SmtpServerConfig config) {
        return new ReactiveSmtpMailSender("1", config, mqScheduler, meterRegistry);
    }
}
