package fr.ght1pc9kc.baywatch.security.domain.ports;

import fr.ght1pc9kc.baywatch.security.api.model.AuthenticationRequest;
import fr.ght1pc9kc.baywatch.security.api.model.BaywatchAuthentication;
import reactor.core.publisher.Mono;

public interface AuthenticationManagerPort {
    Mono<BaywatchAuthentication> authenticate(AuthenticationRequest request);
}
