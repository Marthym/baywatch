package fr.ght1pc9kc.baywatch.security.infra.adapters;

import com.github.f4b6a3.ulid.UlidFactory;
import fr.ght1pc9kc.baywatch.common.api.model.UserMeta;
import fr.ght1pc9kc.baywatch.common.infra.filters.ReactiveClientInfoContextHolder;
import fr.ght1pc9kc.baywatch.security.api.AuthenticationFacade;
import fr.ght1pc9kc.baywatch.security.api.AuthorizationService;
import fr.ght1pc9kc.baywatch.security.api.PasswordService;
import fr.ght1pc9kc.baywatch.security.api.UserService;
import fr.ght1pc9kc.baywatch.security.domain.UserServiceImpl;
import fr.ght1pc9kc.baywatch.security.domain.ports.AuthorizationPersistencePort;
import fr.ght1pc9kc.baywatch.security.domain.ports.NotificationPort;
import fr.ght1pc9kc.baywatch.security.domain.ports.UserPersistencePort;
import fr.ght1pc9kc.baywatch.security.infra.model.BaywatchUserDetails;
import fr.ght1pc9kc.juery.api.Criteria;
import fr.ght1pc9kc.juery.api.PageRequest;
import lombok.AccessLevel;
import lombok.Setter;
import lombok.experimental.Delegate;
import org.jetbrains.annotations.VisibleForTesting;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Clock;
import java.time.temporal.ChronoUnit;

import static fr.ght1pc9kc.baywatch.common.api.model.EntitiesProperties.LOGIN;

@Service
@Qualifier("Baywatch")
public class UserServiceAdapter implements AuthorizationService, UserService, ReactiveUserDetailsService {
    @Delegate
    private final UserService delegate;
    @Delegate
    private final AuthorizationService delegateA;

    @Setter(value = AccessLevel.PACKAGE, onMethod = @__(@VisibleForTesting))
    private Clock clock = Clock.systemUTC();

    @Autowired
    public UserServiceAdapter(UserPersistencePort userPersistencePort,
                              AuthorizationPersistencePort authorizationRepository,
                              NotificationPort notificationPort,
                              AuthenticationFacade authFacade,
                              PasswordService passwordService) {
        this.delegate = new UserServiceImpl(
                userPersistencePort, authorizationRepository, notificationPort, authFacade, passwordService,
                Clock.systemUTC(), UlidFactory.newMonotonicInstance());
        this.delegateA = (UserServiceImpl) this.delegate;
    }

    @Override
    public Mono<UserDetails> findByUsername(String username) {
        return Mono.just(username).flatMapMany(u ->
                        delegate.list(PageRequest.one(Criteria.property(LOGIN).eq(username)))
                                .contextWrite(AuthenticationFacade.withSystemAuthentication()))
                .single()
                .flatMap(user -> ReactiveClientInfoContextHolder.getContext()
                        .map(clientInfo -> user.withMeta(UserMeta.loginIP, clientInfo.ip().toString())
                                .withMeta(UserMeta.loginAt, clock.instant().truncatedTo(ChronoUnit.SECONDS)))
                        .switchIfEmpty(Mono.just(user)))
                .map(BaywatchUserDetails::new);
    }
}
