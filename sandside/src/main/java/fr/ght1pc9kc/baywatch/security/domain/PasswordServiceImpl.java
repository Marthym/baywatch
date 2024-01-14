package fr.ght1pc9kc.baywatch.security.domain;

import fr.ght1pc9kc.baywatch.common.api.LocaleFacade;
import fr.ght1pc9kc.baywatch.common.api.exceptions.UnauthorizedException;
import fr.ght1pc9kc.baywatch.security.api.AuthenticationFacade;
import fr.ght1pc9kc.baywatch.security.api.PasswordService;
import fr.ght1pc9kc.baywatch.security.api.model.PasswordEvaluation;
import fr.ght1pc9kc.baywatch.security.api.model.User;
import fr.ght1pc9kc.baywatch.security.domain.ports.PasswordStrengthChecker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.stream.LongStream;

@Slf4j
@RequiredArgsConstructor
public class PasswordServiceImpl implements PasswordService {

    private final AuthenticationFacade authFacade;
    private final PasswordStrengthChecker passwordChecker;
    private final LocaleFacade localeFacade;

    @Override
    public Mono<PasswordEvaluation> checkPasswordStrength(String password) {
        return authFacade.getConnectedUser()
                .switchIfEmpty(Mono.error(UnauthorizedException::new))
                .map(user -> user.self().withPassword(password))
                .flatMap(this::checkPasswordStrength);
    }

    @Override
    public Mono<PasswordEvaluation> checkPasswordStrength(User user) {
        return localeFacade.getLocale()
                .map(locale -> passwordChecker.estimate(user.password, locale, List.of(user.name, user.login, user.mail)));
    }

    @Override
    public Flux<String> generateSecurePassword(int number) {
        if (number > 100 || number < 1) {
            return Flux.error(() -> new IllegalArgumentException("Invalid number of passwords required !"));
        }
        return Flux.<String>create(sink ->
                        sink.onRequest(n -> LongStream.range(0, number)
                                .mapToObj(ignore -> passwordChecker.generate())
                                .forEach(sink::next)))
                .take(number);
    }
}
