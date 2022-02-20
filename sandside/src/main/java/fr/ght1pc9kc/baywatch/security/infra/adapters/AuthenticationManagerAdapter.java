package fr.ght1pc9kc.baywatch.security.infra.adapters;

import fr.ght1pc9kc.baywatch.security.api.AuthenticationService;
import fr.ght1pc9kc.baywatch.security.domain.ports.JwtTokenProvider;
import fr.ght1pc9kc.baywatch.security.domain.AuthenticationServiceImpl;
import lombok.experimental.Delegate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UserDetailsRepositoryReactiveAuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class AuthenticationManagerAdapter implements ReactiveAuthenticationManager, AuthenticationService {

    @Delegate
    private final ReactiveAuthenticationManager delegate;

    @Delegate
    private final AuthenticationService delegateAuthService;

    @Autowired
    public AuthenticationManagerAdapter(
            @Qualifier("Baywatch") UserServiceAdapter userDetailsService,
            JwtTokenProvider tokenProvider,
            PasswordEncoder passwordEncoder) {
        UserDetailsRepositoryReactiveAuthenticationManager authManager =
                new UserDetailsRepositoryReactiveAuthenticationManager(userDetailsService);
        authManager.setPasswordEncoder(passwordEncoder);
        this.delegate = authManager;
        this.delegateAuthService = new AuthenticationServiceImpl(tokenProvider, userDetailsService);
    }

}
