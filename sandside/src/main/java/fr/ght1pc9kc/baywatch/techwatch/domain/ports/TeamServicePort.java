package fr.ght1pc9kc.baywatch.techwatch.domain.ports;

import reactor.core.publisher.Flux;

public interface TeamServicePort {
    Flux<String> getTeamMates(String userId);
}
