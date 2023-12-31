package fr.ght1pc9kc.baywatch.security.api;

import fr.ght1pc9kc.baywatch.security.api.model.BaywatchAuthentication;
import reactor.core.publisher.Mono;

public interface AuthenticationService {
    Mono<BaywatchAuthentication> refresh(String token);
}
