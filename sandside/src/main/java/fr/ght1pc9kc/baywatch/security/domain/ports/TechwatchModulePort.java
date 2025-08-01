package fr.ght1pc9kc.baywatch.security.domain.ports;

import fr.ght1pc9kc.baywatch.security.domain.model.PersonalFeed;
import reactor.core.publisher.Mono;

public interface TechwatchModulePort {
    Mono<Void> addAndSubscribePersonalFeed(PersonalFeed personalFeed);
}
