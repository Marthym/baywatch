package fr.ght1pc9kc.baywatch.security.domain;

import com.machinezoo.noexception.Exceptions;
import fr.ght1pc9kc.baywatch.common.api.model.UserMeta;
import fr.ght1pc9kc.baywatch.security.api.AuthenticationService;
import fr.ght1pc9kc.baywatch.security.api.UserService;
import fr.ght1pc9kc.baywatch.security.api.model.AuthenticationRequest;
import fr.ght1pc9kc.baywatch.security.api.model.BaywatchAuthentication;
import fr.ght1pc9kc.baywatch.security.domain.ports.AuthenticationManagerPort;
import fr.ght1pc9kc.baywatch.security.domain.ports.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

import java.net.InetAddress;
import java.util.Collections;

@Slf4j
@RequiredArgsConstructor
public class AuthenticationServiceImpl implements AuthenticationService {
    private final AuthenticationManagerPort authenticationManagerPort;
    private final JwtTokenProvider tokenProvider;
    private final UserService userService;

    @Override
    public Mono<BaywatchAuthentication> login(AuthenticationRequest login) {
        return authenticationManagerPort.authenticate(login)
                .map(auth -> tokenProvider.createToken(auth.user(), auth.rememberMe(), auth.authorities()));
    }

    @Override
    public Mono<BaywatchAuthentication> refresh(String token) {
        return Mono.fromCallable(() -> tokenProvider.getAuthentication(token))
                .flatMap(auth -> userService.get(auth.user().id())
                        .map(user -> tokenProvider.createToken(user, auth.rememberMe(), Collections.emptyList())))
                .doOnSuccess(auth -> {
                    if (log.isDebugEnabled()) {
                        InetAddress clientIp = auth.user().meta(UserMeta.loginIP)
                                .flatMap(Exceptions.silence().function(Exceptions.sneak().function(InetAddress::getByName)))
                                .orElse(Exceptions.sneak().get(() -> InetAddress.getByName("127.0.0.1")));
                        log.debug("Login to {} from {}.", auth.user().self().login(), clientIp);
                    }
                });
    }
}