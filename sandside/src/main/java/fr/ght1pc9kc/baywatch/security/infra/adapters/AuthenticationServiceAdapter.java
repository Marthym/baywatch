package fr.ght1pc9kc.baywatch.security.infra.adapters;

import fr.ght1pc9kc.baywatch.security.api.AuthenticationFacade;
import fr.ght1pc9kc.baywatch.security.api.AuthenticationService;
import fr.ght1pc9kc.baywatch.security.api.UserService;
import fr.ght1pc9kc.baywatch.security.domain.AuthenticationServiceImpl;
import fr.ght1pc9kc.baywatch.security.domain.ports.AuthenticationManagerPort;
import fr.ght1pc9kc.baywatch.security.domain.ports.JwtTokenProvider;
import fr.ght1pc9kc.baywatch.security.domain.ports.MailSenderPort;
import lombok.experimental.Delegate;
import org.springframework.stereotype.Service;

@Service
public class AuthenticationServiceAdapter implements AuthenticationService {
    @Delegate
    private final AuthenticationServiceImpl delegate;

    public AuthenticationServiceAdapter(
            AuthenticationManagerPort authenticationManagerPort, JwtTokenProvider tokenProvider, UserService userService,
            AuthenticationFacade authFacade, MailSenderPort mailSender) {
        this.delegate = new AuthenticationServiceImpl(authenticationManagerPort, tokenProvider, userService, authFacade, mailSender);
        this.delegate.onPostConstruct();
    }
}
