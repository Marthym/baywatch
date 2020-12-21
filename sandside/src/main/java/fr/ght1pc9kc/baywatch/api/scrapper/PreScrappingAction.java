package fr.ght1pc9kc.baywatch.api.scrapper;

import reactor.core.publisher.Mono;

public interface PreScrappingAction {
    Mono<Void> call();
}
