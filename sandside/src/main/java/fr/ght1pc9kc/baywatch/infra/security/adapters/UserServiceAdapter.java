package fr.ght1pc9kc.baywatch.infra.security.adapters;

import fr.ght1pc9kc.baywatch.api.security.UserService;
import fr.ght1pc9kc.baywatch.domain.security.ports.AuthenticationFacade;
import fr.ght1pc9kc.baywatch.domain.security.ports.UserPersistencePort;
import fr.ght1pc9kc.baywatch.domain.security.UserServiceImpl;
import fr.ght1pc9kc.baywatch.infra.security.model.BaywatchUserDetails;
import fr.ght1pc9kc.juery.api.Criteria;
import fr.ght1pc9kc.juery.api.PageRequest;
import lombok.experimental.Delegate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Clock;

import static fr.ght1pc9kc.baywatch.api.common.model.EntitiesProperties.LOGIN;

@Service
@Qualifier("Baywatch")
public class UserServiceAdapter implements UserService, ReactiveUserDetailsService {
    @Delegate
    private final UserService delegate;
    private final AuthenticationFacade authFacade;

    @Autowired
    public UserServiceAdapter(UserPersistencePort userPersistencePort, AuthenticationFacade authFacade,
                              PasswordEncoder passwordEncoder) {
        this.delegate = new UserServiceImpl(userPersistencePort, authFacade, passwordEncoder, Clock.systemUTC());
        this.authFacade = authFacade;
    }

    @Override
    public Mono<UserDetails> findByUsername(String username) {
        return Mono.just(username).flatMapMany(u ->
                        delegate.list(PageRequest.one(Criteria.property(LOGIN).eq(username)))
                                .contextWrite(authFacade.withSystemAuthentication()))
                .single().map(BaywatchUserDetails::new);
    }
}
