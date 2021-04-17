package fr.ght1pc9kc.baywatch.api;

import fr.ght1pc9kc.baywatch.api.security.model.BaywatchAuthentication;
import reactor.core.publisher.Mono;
import reactor.util.context.Context;

public interface AuthenticationService {
    Mono<BaywatchAuthentication> refresh(String token);

    Context withSystemAuthentication();
}
