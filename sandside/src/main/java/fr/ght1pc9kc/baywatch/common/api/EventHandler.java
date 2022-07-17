package fr.ght1pc9kc.baywatch.common.api;

import reactor.core.publisher.Mono;

import java.util.Set;

/**
 * The Scrapping Handlers allow inserting action before and after the News Scrapping
 */
public interface EventHandler {
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
    default Mono<Void> after(int persisted) {
        return Mono.empty().then();
    }

    default void onTerminate() {
    }

    Set<String> eventTypes();
}
