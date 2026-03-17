package fr.ght1pc9kc.baywatch.security.infra.adapters.services;

import fr.ght1pc9kc.baywatch.security.api.AuthenticationFacade;
import fr.ght1pc9kc.baywatch.security.api.PasswordChecker;
import fr.ght1pc9kc.baywatch.security.api.PasswordResetService;
import fr.ght1pc9kc.baywatch.security.api.UserService;
import fr.ght1pc9kc.baywatch.security.domain.PasswordResetServiceImpl;
import fr.ght1pc9kc.baywatch.security.domain.ports.MailSenderPort;
import fr.ght1pc9kc.baywatch.security.domain.ports.KeyValuePersistencePort;
import lombok.experimental.Delegate;
import org.springframework.stereotype.Service;

@Service
public class PasswordResetServiceAdapter implements PasswordResetService {
    @Delegate
    private final PasswordResetServiceImpl delegate;

    public PasswordResetServiceAdapter(
            AuthenticationFacade authFacade, KeyValuePersistencePort keyValuePersistencePort, UserService userService,
            PasswordChecker passwordChecker, MailSenderPort mailSender) {
        this.delegate = new PasswordResetServiceImpl(authFacade, userService, passwordChecker, mailSender, keyValuePersistencePort);
    }
}
