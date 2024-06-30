package fr.ght1pc9kc.baywatch.security.api;

import fr.ght1pc9kc.baywatch.security.api.model.AuthenticationRequest;
import fr.ght1pc9kc.baywatch.security.api.model.BaywatchAuthentication;
import reactor.core.publisher.Mono;

public interface AuthenticationService {
    Mono<BaywatchAuthentication> login(AuthenticationRequest authRequest);

    Mono<BaywatchAuthentication> refresh(String token);
}
