package fr.ght1pc9kc.baywatch.api.security;

import fr.ght1pc9kc.baywatch.api.security.model.BaywatchAuthentication;
import reactor.core.publisher.Mono;

public interface AuthenticationService {
    Mono<BaywatchAuthentication> refresh(String token);
}
