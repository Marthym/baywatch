package fr.ght1pc9kc.baywatch.notify.domain.services;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.f4b6a3.ulid.UlidFactory;
import fr.ght1pc9kc.baywatch.common.api.model.TemplateVariable;
import fr.ght1pc9kc.baywatch.notify.api.MailClient;
import fr.ght1pc9kc.baywatch.notify.api.model.MailTemplateName;
import fr.ght1pc9kc.baywatch.notify.domain.MailTemplateService;
import fr.ght1pc9kc.baywatch.notify.domain.exceptions.MailLimitExceededException;
import fr.ght1pc9kc.baywatch.notify.domain.model.Mail;
import fr.ght1pc9kc.baywatch.notify.domain.model.MailMeta;
import fr.ght1pc9kc.baywatch.notify.domain.ports.MailQueuePersistencePort;
import fr.ght1pc9kc.baywatch.notify.domain.ports.NotifyAuthenticationPort;
import fr.ght1pc9kc.baywatch.notify.domain.ports.NotifyClientInfoPort;
import fr.ght1pc9kc.entity.api.Entity;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.apache.commons.text.CaseUtils;
import org.apache.commons.text.StringSubstitutor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.VisibleForTesting;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuples;

import java.net.URI;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static fr.ght1pc9kc.baywatch.notify.domain.model.NotifyConstants.MATE_PREFIX;

@RequiredArgsConstructor
public class MailClientImpl implements MailClient {
    private static final int MAX_MAIL_TO_ADDRESS = 5;
    private final MailTemplateService templateService;
    private final NotifyAuthenticationPort authenticationPort;
    private final NotifyClientInfoPort localeFacade;
    private final MailQueuePersistencePort queuePersistencePort;
    private final Set<String> whitelistIps;

    private final UlidFactory ulidFactory = UlidFactory.newMonotonicInstance();

    private final Cache<@NotNull String, AtomicInteger> limiter = Caffeine.newBuilder()
            .expireAfterAccess(Duration.ofHours(6))
            .build();

    @Setter(onMethod_ = @__(@VisibleForTesting))
    private Clock clock = Clock.systemUTC();

    @Override
    public Mono<Void> send(MailTemplateName template, String to, EnumMap<TemplateVariable, String> variables) {
        Instant now = clock.instant();
        return localeFacade.getRemoteAddress()
                .handle((ip, sink) -> {
                    String hostAddress = ip.getAddress().getHostAddress();
                    if (whitelistIps.contains(hostAddress)) {
                        sink.next(ip);
                        return;
                    }
                    int count = Objects.requireNonNull(limiter.get(hostAddress + "@" + to, k -> new AtomicInteger(1)))
                            .getAndIncrement();
                    if (count > MAX_MAIL_TO_ADDRESS) {
                        sink.error(new MailLimitExceededException(String.format("Too many mail send to %s", to)));
                    } else {
                        sink.next(ip);
                    }

                }).flatMap(ignore -> Mono.zip(
                        localeFacade.getLocale()
                                .map(currentLocale -> localeFacade.getAvailableLanguages().stream()
                                        .filter(l -> l.getLanguage().equals(currentLocale.getLanguage()))
                                        .min(Comparator.comparing(Locale::getCountry, Comparator.nullsLast(Comparator.reverseOrder())))
                                        .orElse(localeFacade.getAvailableLanguages().getFirst())),
                        authenticationPort.getConnectedUser()
                                .switchIfEmpty(Mono.error(() -> new IllegalStateException("Need user in context to send mail")))
                ))

                .flatMap(t -> templateService.get(template, t.getT1())
                        .map(mailTemplate -> Tuples.of(t.getT2(), mailTemplate)))

                .flatMap(t -> localeFacade.getBaseUrl()
                        .switchIfEmpty(Mono.just(URI.create("http://localhost")))
                        .map(baseUrl -> {
                    var collectedTemplateVariables = new EnumMap<>(t.getT1().self());
                    collectedTemplateVariables.putAll(t.getT1().self());
                    collectedTemplateVariables.put(TemplateVariable.BASE_URL, String.format("%s://%s", baseUrl.getScheme(), baseUrl.getAuthority()));
                    collectedTemplateVariables.putAll(variables);
                    return Tuples.of(t.getT1().convert(ignore ->
                            Map.copyOf(collectedTemplateVariables)), t.getT2());

                })).map(t -> {
                    Map<String, String> valueMap = t.getT1().self().entrySet().stream()
                            .map(e -> Map.entry(
                                    CaseUtils.toCamelCase(e.getKey().name(), false, '_'),
                                    e.getValue())).collect(Collectors.toUnmodifiableMap(Map.Entry::getKey, Map.Entry::getValue));
                    StringSubstitutor interpolator = new StringSubstitutor(valueMap);
                    return Entity.identify(Mail.builder()
                                    .to(to)
                                    .subject(interpolator.replace(t.getT2().subject()))
                                    .message(interpolator.replace(t.getT2().body()))
                                    .build())
                            .meta(MailMeta.createdAt, now)
                            .meta(MailMeta.createdBy, t.getT1().id())
                            .meta(MailMeta.template, t.getT2().id().name())
                            .withId(MATE_PREFIX + ulidFactory.create().toString());
                })
                .flatMap(queuePersistencePort::push);
    }
}
