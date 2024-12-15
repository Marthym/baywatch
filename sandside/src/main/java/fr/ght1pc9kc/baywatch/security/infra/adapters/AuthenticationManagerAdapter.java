package fr.ght1pc9kc.baywatch.security.infra.adapters;

import fr.ght1pc9kc.baywatch.security.api.model.AuthenticationRequest;
import fr.ght1pc9kc.baywatch.security.api.model.BaywatchAuthentication;
import fr.ght1pc9kc.baywatch.security.domain.ports.AuthenticationManagerPort;
import fr.ght1pc9kc.baywatch.security.infra.model.BaywatchUserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UserDetailsRepositoryReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class AuthenticationManagerAdapter implements AuthenticationManagerPort, ReactiveAuthenticationManager {
    private final ReactiveAuthenticationManager delegate;

    @Autowired
    public AuthenticationManagerAdapter(
            @Qualifier("Baywatch") UserServiceAdapter userDetailsService,
            PasswordEncoder passwordEncoder) {
        UserDetailsRepositoryReactiveAuthenticationManager authManager =
                new UserDetailsRepositoryReactiveAuthenticationManager(userDetailsService);
        authManager.setPasswordEncoder(passwordEncoder);
        this.delegate = authManager;
    }

    @Override
    public Mono<BaywatchAuthentication> authenticate(AuthenticationRequest request) {
        return delegate.authenticate(new UsernamePasswordAuthenticationToken(request.username(), request.password()))
                .map(auth -> new BaywatchAuthentication(
                        ((BaywatchUserDetails) auth.getPrincipal()).entity(),
                        null,
                        request.rememberMe(),
                        AuthorityUtils.authorityListToSet(auth.getAuthorities())
                ));
    }

    @Override
    public Mono<Authentication> authenticate(Authentication authentication) {
        return delegate.authenticate(authentication);
    }
}
