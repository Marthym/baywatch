package fr.ght1pc9kc.baywatch.domain.security;

import fr.ght1pc9kc.baywatch.api.AuthenticationService;
import fr.ght1pc9kc.baywatch.api.UserService;
import fr.ght1pc9kc.baywatch.api.model.BaywatchAuthentication;
import fr.ght1pc9kc.baywatch.domain.ports.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

import java.util.Collections;

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
                                .map(user -> {
                                    String refreshedToken = tokenProvider.createToken(user, Collections.emptyList());
                                    return new BaywatchAuthentication(user, refreshedToken, Collections.emptyList());
                                });
                    }
                });
    }
}