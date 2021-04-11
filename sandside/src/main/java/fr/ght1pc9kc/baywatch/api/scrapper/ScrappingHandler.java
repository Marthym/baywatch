package fr.ght1pc9kc.baywatch.api.scrapper;

import reactor.core.publisher.Mono;

/**
 * The Scrapping Handlers allow to insert action before and after the News Scrapping
 */
public interface ScrappingHandler {
    /**
     * This was run before News Scrapping
     *
     * @return nothing
     */
    default Mono<Void> before() {
        return Mono.empty().then();
    }

    /**
     * This was run after News Scrapping
     *
     * @return nothing
     */
    default Mono<Void> after() {
        return Mono.empty().then();
    }
}
