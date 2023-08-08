package fr.ght1pc9kc.baywatch.security.domain;

import fr.ght1pc9kc.baywatch.common.api.LocaleFacade;
import fr.ght1pc9kc.baywatch.common.api.exceptions.UnauthorizedException;
import fr.ght1pc9kc.baywatch.security.api.AuthenticationFacade;
import fr.ght1pc9kc.baywatch.security.api.PasswordService;
import fr.ght1pc9kc.baywatch.security.api.model.PasswordEvaluation;
import fr.ght1pc9kc.baywatch.security.domain.ports.PasswordStrengthChecker;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuples;

import java.util.List;

@RequiredArgsConstructor
public class PasswordServiceImpl implements PasswordService {

    private final AuthenticationFacade authFacade;
    private final PasswordStrengthChecker passwordChecker;
    private final LocaleFacade localeFacade;

    @Override
    public Mono<PasswordEvaluation> checkPasswordStrength(String password) {
        return authFacade.getConnectedUser()
                .switchIfEmpty(Mono.error(UnauthorizedException::new))
                .flatMap(user -> localeFacade.getLocale().map(l -> Tuples.of(user, l)))
                .map(context ->
                        passwordChecker.estimate(password, context.getT2(), List.of(
                                context.getT1().self.name,
                                context.getT1().self.login,
                                context.getT1().self.mail))
                );
    }

    @Override
    public Mono<String> generateSecurePassword() {
        return null;
    }
}
