package fr.ght1pc9kc.baywatch.notify.domain.services;

import com.github.f4b6a3.ulid.UlidFactory;
import fr.ght1pc9kc.baywatch.notify.api.MailClient;
import fr.ght1pc9kc.baywatch.notify.api.model.MailTemplateName;
import fr.ght1pc9kc.baywatch.notify.domain.MailTemplateService;
import fr.ght1pc9kc.baywatch.notify.domain.model.Mail;
import fr.ght1pc9kc.baywatch.notify.domain.model.MailContextVariables;
import fr.ght1pc9kc.baywatch.notify.domain.model.MailMeta;
import fr.ght1pc9kc.baywatch.notify.domain.ports.MailQueuePersistencePort;
import fr.ght1pc9kc.baywatch.notify.domain.ports.NotifyAuthenticationPort;
import fr.ght1pc9kc.baywatch.notify.domain.ports.NotifyLocaleFacadePort;
import fr.ght1pc9kc.entity.api.Entity;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.apache.commons.text.StringSubstitutor;
import org.jetbrains.annotations.VisibleForTesting;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuples;

import java.time.Clock;
import java.time.Instant;

import static fr.ght1pc9kc.baywatch.notify.domain.model.NotifyConstants.MATE_PREFIX;

@RequiredArgsConstructor
public class MailClientImpl implements MailClient {
    private final MailTemplateService templateService;
    private final NotifyAuthenticationPort authenticationPort;
    private final NotifyLocaleFacadePort localeFacade;
    private final MailQueuePersistencePort queuePersistencePort;
    private final UlidFactory ulidFactory = UlidFactory.newMonotonicInstance();

    @Setter(onMethod_ = @__(@VisibleForTesting))
    private Clock clock = Clock.systemUTC();

    @Override
    public Mono<Void> send(MailTemplateName template, String to, MailContextVariables variables) {
        Instant now = clock.instant();
        return Mono.zip(
                        localeFacade.getLocale(),
                        authenticationPort.getConnectedUser()
                                .map(ctx -> ctx.convert(self -> variables.toBuilder()
                                        .username(self.username())
                                        .login(self.login())
                                        .build()))
                )

                .flatMap(t -> templateService.get(template, t.getT1())
                        .map(mailTemplate -> Tuples.of(t.getT2(), mailTemplate)))

                .map(t -> {
                    StringSubstitutor interpolator = new StringSubstitutor(t.getT1().self().asMap());
                    return Entity.identify(Mail.builder()
                                    .to(to)
                                    .subject(interpolator.replace(t.getT2().self().subject()))
                                    .message(interpolator.replace(t.getT2().self().body()))
                                    .build())
                            .meta(MailMeta.createdAt, now)
                            .meta(MailMeta.createdBy, t.getT1().id())
                            .meta(MailMeta.template, t.getT2().id())
                            .withId(MATE_PREFIX + ulidFactory.create().toString());
                })
                .flatMap(queuePersistencePort::push);
    }
}
