package fr.ght1pc9kc.baywatch.infra.security.adapters;

import fr.ght1pc9kc.baywatch.api.AuthenticationService;
import fr.ght1pc9kc.baywatch.api.model.Role;
import fr.ght1pc9kc.baywatch.api.model.User;
import fr.ght1pc9kc.baywatch.domain.ports.JwtTokenProvider;
import fr.ght1pc9kc.baywatch.domain.security.AuthenticationServiceImpl;
import fr.ght1pc9kc.baywatch.infra.adapters.UserServiceAdapter;
import lombok.experimental.Delegate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UserDetailsRepositoryReactiveAuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.stereotype.Component;
import reactor.util.context.Context;

@Component
public class AuthenticationManagerAdapter implements ReactiveAuthenticationManager, AuthenticationService {

    @Delegate
    private final ReactiveAuthenticationManager delegate;

    @Delegate(excludes = ExcludeDelegation.class)
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

    @Override
    public Context withSystemAuthentication() {
        User principal = User.builder()
                .id(Role.SYSTEM.name())
                .name(Role.SYSTEM.name())
                .login(Role.SYSTEM.name().toLowerCase())
                .role(Role.SYSTEM).build();
        Authentication authentication = new PreAuthenticatedAuthenticationToken(principal, null,
                AuthorityUtils.createAuthorityList(Role.SYSTEM.name()));
        return ReactiveSecurityContextHolder.withAuthentication(authentication);
    }

    private interface ExcludeDelegation {
        Context withSystemAuthentication();
    }
}
