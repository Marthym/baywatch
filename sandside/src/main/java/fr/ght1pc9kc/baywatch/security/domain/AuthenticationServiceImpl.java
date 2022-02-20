package fr.ght1pc9kc.baywatch.security.domain;

import fr.ght1pc9kc.baywatch.security.api.AuthenticationService;
import fr.ght1pc9kc.baywatch.security.api.UserService;
import fr.ght1pc9kc.baywatch.security.api.model.BaywatchAuthentication;
import fr.ght1pc9kc.baywatch.security.domain.ports.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public class AuthenticationServiceImpl implements AuthenticationService {
    private final JwtTokenProvider tokenProvider;
    private final UserService userService;

    @Override
    public Mono<BaywatchAuthentication> refresh(String token) {
        return Mono.fromCallable(() -> tokenProvider.getAuthentication(token))
                .flatMap(auth -> {
                    if (tokenProvider.validateToken(token)) {
                        return Mono.just(auth);
                    } else {
                        return userService.get(auth.user.id)
                                .map(user -> tokenProvider.createToken(user, auth.rememberMe, auth.getAuthorities()));
                    }
                });
    }
}