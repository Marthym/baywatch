package fr.ght1pc9kc.baywatch.infra.security;

import fr.ght1pc9kc.baywatch.api.model.request.filter.Criteria;
import fr.ght1pc9kc.baywatch.domain.ports.UserPersistencePort;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class BaywatchUserDetailsService implements ReactiveUserDetailsService {
    private final UserPersistencePort userRepository;

    @Override
    public Mono<UserDetails> findByUsername(String username) {
        return userRepository.list(Criteria.property("login").eq(username))
                .next()
                .map(user -> User.withUsername(user.login).build());
    }
}
