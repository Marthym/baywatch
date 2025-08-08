package fr.ght1pc9kc.baywatch.notify.infra.controllers;

import fr.ght1pc9kc.baywatch.notify.domain.ports.MailQueuePersistencePort;
import fr.ght1pc9kc.baywatch.notify.domain.ports.SmtpConfigurationPort;
import fr.ght1pc9kc.baywatch.notify.infra.adapters.ReactiveSmtpMailSender;
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
    private final Scheduler mailQueueScheduler = Schedulers.newBoundedElastic(
            4, Integer.MAX_VALUE, "mailQueue-", 10, true);

    private final SmtpConfigurationPort smtpConfigurationPort;
    private final MeterRegistry meterRegistry;
    private final MailQueuePersistencePort mailQueuePersistencePort;

    @Async
    @EventListener(ApplicationStartedEvent.class)
    public void startMailQueue() {
        log.atDebug().log("Start mail queue in 2 minutes ...");
        scheduleExecutor.schedule(this, 2, TimeUnit.MINUTES);
    }

    @PreDestroy
    @SneakyThrows
    public void shutdownScrapping() {
        log.atInfo().log("Mail queue complete and shutdown !");
    }

    @Override
    public void run() {
        smtpConfigurationPort.get()
                .switchIfEmpty(Mono.fromCallable(() -> {
                    log.atWarn().log("No SMTP configuration found, stoping mail queue.");
                    return null;
                }))
                .map(config -> new ReactiveSmtpMailSender("1", config, mailQueueScheduler, meterRegistry))
                .flatMapMany(sender -> mailQueuePersistencePort.consume()
                        .flatMap(mail -> sender.sendMail(mail.self())))
                .subscribe(smtpResult -> {
                    scheduleExecutor.schedule(this, 2, TimeUnit.MINUTES);
                    if (smtpResult.isFailure()) {
                        log.atError()
                                .addArgument(smtpResult.getCause().getCause())
                                .addArgument(smtpResult.getCause().getMessage())
                                .log("Error sending mail: {}: {}", smtpResult.getCause());
                        log.atDebug().log(STACKTRACE, smtpResult.getCause());
                    }
                }, error -> {
                    log.atError()
                            .addArgument(error.getCause())
                            .addArgument(error.getMessage())
                            .log("Mail queue stop with error -> {}: {}");
                    log.atDebug().log(STACKTRACE, error);
                });
    }
}
