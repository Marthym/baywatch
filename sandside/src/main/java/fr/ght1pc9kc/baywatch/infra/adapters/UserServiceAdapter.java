package fr.ght1pc9kc.baywatch.infra.adapters;

import fr.ght1pc9kc.baywatch.api.UserService;
import fr.ght1pc9kc.baywatch.api.model.request.PageRequest;
import fr.ght1pc9kc.baywatch.api.model.request.filter.Criteria;
import fr.ght1pc9kc.baywatch.domain.UserServiceImpl;
import fr.ght1pc9kc.baywatch.domain.ports.UserPersistencePort;
import lombok.experimental.Delegate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
public class UserServiceAdapter implements UserService, ReactiveUserDetailsService {
    @Delegate
    private final UserService delegate;

    @Autowired
    public UserServiceAdapter(UserPersistencePort userPersistencePort) {
        this.delegate = new UserServiceImpl(userPersistencePort);
    }

    @Override
    public Mono<UserDetails> findByUsername(String username) {
        return delegate.list(PageRequest.one(Criteria.property("login").eq(username)))
                .next()
                .map(user -> User.withUsername(user.login)
                        .password(user.password)
                        .authorities(List.of())
                        .build());
    }
}
