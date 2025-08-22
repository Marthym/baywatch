package fr.ght1pc9kc.baywatch.security.infra.adapters;

import fr.ght1pc9kc.baywatch.common.api.ClientInfoFacade;
import fr.ght1pc9kc.baywatch.security.api.AuthenticationFacade;
import fr.ght1pc9kc.baywatch.security.api.PasswordService;
import fr.ght1pc9kc.baywatch.security.domain.PasswordServiceImpl;
import fr.ght1pc9kc.baywatch.security.domain.ports.PasswordStrengthChecker;
import lombok.experimental.Delegate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class PasswordServiceAdapter implements PasswordService {
    @Delegate
    private final PasswordServiceImpl delegate;

    @Delegate
    private final PasswordEncoder encoder;

    public PasswordServiceAdapter(
            AuthenticationFacade authFacade, PasswordStrengthChecker passwordStrengthChecker, PasswordEncoder encoder,
            ClientInfoFacade clientInfoFacade) {
        this.encoder = encoder;
        this.delegate = new PasswordServiceImpl(authFacade, passwordStrengthChecker, clientInfoFacade);
    }
}
