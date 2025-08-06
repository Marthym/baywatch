package fr.ght1pc9kc.baywatch.notify.domain.services;

import com.github.f4b6a3.ulid.UlidFactory;
import fr.ght1pc9kc.baywatch.common.api.model.TemplateVariable;
import fr.ght1pc9kc.baywatch.notify.api.MailClient;
import fr.ght1pc9kc.baywatch.notify.api.model.MailTemplateName;
import fr.ght1pc9kc.baywatch.notify.domain.MailTemplateService;
import fr.ght1pc9kc.baywatch.notify.domain.model.Mail;
import fr.ght1pc9kc.baywatch.notify.domain.model.MailMeta;
import fr.ght1pc9kc.baywatch.notify.domain.ports.MailQueuePersistencePort;
import fr.ght1pc9kc.baywatch.notify.domain.ports.NotifyAuthenticationPort;
import fr.ght1pc9kc.baywatch.notify.domain.ports.NotifyLocaleFacadePort;
import fr.ght1pc9kc.entity.api.Entity;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.apache.commons.text.CaseUtils;
import org.apache.commons.text.StringSubstitutor;
import org.jetbrains.annotations.VisibleForTesting;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuples;

import java.time.Clock;
import java.time.Instant;
import java.util.EnumMap;
import java.util.Map;
import java.util.stream.Collectors;

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
    public Mono<Void> send(MailTemplateName template, String to, EnumMap<TemplateVariable, String> variables) {
        Instant now = clock.instant();
        return Mono.zip(
                        localeFacade.getLocale(),
                        authenticationPort.getConnectedUser()
                )

                .flatMap(t -> templateService.get(template, t.getT1())
                        .map(mailTemplate -> Tuples.of(t.getT2(), mailTemplate)))

                .map(t -> {
                    Map<String, String> valueMap = t.getT1().self().entrySet().stream()
                            .map(e -> Map.entry(
                                    CaseUtils.toCamelCase(e.getKey().name(), false, '_'),
                                    e.getValue())).collect(Collectors.toUnmodifiableMap(Map.Entry::getKey, Map.Entry::getValue));
                    StringSubstitutor interpolator = new StringSubstitutor(valueMap);
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
