package fr.ght1pc9kc.baywatch.api;

import fr.ght1pc9kc.baywatch.api.model.BaywatchAuthentication;
import reactor.core.publisher.Mono;

public interface AuthenticationService {
    Mono<BaywatchAuthentication> refresh(String token);
}