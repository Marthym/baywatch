package fr.ght1pc9kc.baywatch.security.infra.adapters;

import fr.ght1pc9kc.baywatch.common.api.model.ClientInfoContext;
import fr.ght1pc9kc.baywatch.common.api.model.UserMeta;
import fr.ght1pc9kc.baywatch.common.infra.filters.ReactiveClientInfoContextHolder;
import fr.ght1pc9kc.baywatch.common.infra.filters.ReactiveLocaleContextHolder;
import fr.ght1pc9kc.baywatch.security.api.AuthenticationFacade;
import fr.ght1pc9kc.baywatch.security.api.model.Permission;
import fr.ght1pc9kc.baywatch.security.api.model.User;
import fr.ght1pc9kc.baywatch.security.domain.ports.UserEventPublisherPort;
import fr.ght1pc9kc.entity.api.Entity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.stereotype.Component;
import reactor.core.Disposable;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;
import reactor.util.context.Context;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import java.util.Locale;
import java.util.NoSuchElementException;
import java.util.function.Function;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserEventPublisherAdapter implements UserEventPublisherPort {
    private final AuthenticationFacade authFacade;

    private final Sinks.Many<Tuple2<Entity<User>, ClientInfoContext>> sink = Sinks.many().multicast().onBackpressureBuffer();

    public Mono<Entity<User>> publish(Entity<User> user) {
        return authFacade.getClientInfoContext()
                .switchIfEmpty(Mono.error(() -> new NoSuchElementException("No context found for user " + user.id())))
                .map(ctx -> sink.tryEmitNext(Tuples.of(user, ctx)))
                .thenReturn(user);
    }

    public Disposable onEvent(Function<Entity<User>, Mono<Void>> mapper) {
        return sink.asFlux().flatMap(event -> {
                    Entity<User> user = event.getT1();

                    // Rebuild authentication with user roles
                    Authentication authentication = new PreAuthenticatedAuthenticationToken(user, null,
                            AuthorityUtils.createAuthorityList(user.self().roles().stream()
                                    .map(Permission::toString)
                                    .toArray(String[]::new)));

                    Locale locale = user.meta(UserMeta.locale, Locale.class).orElse(Locale.getDefault());

                    Context context = ReactiveClientInfoContextHolder.withClientInfo(event.getT2().ip(), event.getT2().userAgent(), event.getT2().baseUrl())
                            .putAll(ReactiveSecurityContextHolder.withAuthentication(authentication).readOnly())
                            .putAll(ReactiveLocaleContextHolder.withLocale(locale).readOnly());

                    return mapper.apply(user).contextWrite(context);
                })
                .onErrorResume(t -> {
                    log.error("Failed to send welcome mail ! -> {}: {}", t.getClass(), t.getLocalizedMessage());
                    log.debug("STACKTRACE", t);
                    return Mono.empty().then();
                }).subscribe();
    }
}
