package fr.ght1pc9kc.baywatch.security.infra.adapters;

import com.github.f4b6a3.ulid.UlidFactory;
import fr.ght1pc9kc.baywatch.security.api.AuthenticationFacade;
import fr.ght1pc9kc.baywatch.security.api.AuthorizationService;
import fr.ght1pc9kc.baywatch.security.api.UserService;
import fr.ght1pc9kc.baywatch.security.domain.UserServiceImpl;
import fr.ght1pc9kc.baywatch.security.domain.ports.AuthorizationPersistencePort;
import fr.ght1pc9kc.baywatch.security.domain.ports.PasswordStrengthChecker;
import fr.ght1pc9kc.baywatch.security.domain.ports.UserPersistencePort;
import fr.ght1pc9kc.baywatch.security.infra.model.BaywatchUserDetails;
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

import static fr.ght1pc9kc.baywatch.common.api.model.EntitiesProperties.LOGIN;

@Service
@Qualifier("Baywatch")
public class UserServiceAdapter implements AuthorizationService, UserService, ReactiveUserDetailsService {
    @Delegate
    private final UserServiceImpl delegate;

    @Autowired
    public UserServiceAdapter(UserPersistencePort userPersistencePort,
                              AuthorizationPersistencePort authorizationRepository,
                              AuthenticationFacade authFacade,
                              PasswordEncoder passwordEncoder, PasswordStrengthChecker passwordChecker) {
        this.delegate = new UserServiceImpl(
                userPersistencePort, authorizationRepository, authFacade, passwordEncoder, Clock.systemUTC(),
                UlidFactory.newInstance(), passwordChecker);
    }

    @Override
    public Mono<UserDetails> findByUsername(String username) {
        return Mono.just(username).flatMapMany(u ->
                        delegate.list(PageRequest.one(Criteria.property(LOGIN).eq(username)))
                                .contextWrite(AuthenticationFacade.withSystemAuthentication()))
                .single().map(BaywatchUserDetails::new);
    }
}
