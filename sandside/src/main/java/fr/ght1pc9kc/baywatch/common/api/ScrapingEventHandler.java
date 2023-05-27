package fr.ght1pc9kc.baywatch.common.api;

import fr.ght1pc9kc.baywatch.scraper.api.model.ScrapResult;
import reactor.core.publisher.Mono;

import java.util.Set;

/**
 * The Scrapping Handlers allow inserting action before and after some type of events
 */
public interface ScrapingEventHandler {
    /**
     * This was run before the event
     *
     * @return nothing but wait the and of action to continue the event
     */
    default Mono<Void> before() {
        return Mono.empty().then();
    }

    /**
     * This was run after the event if something as change
     *
     * @param result The result of the scraping session
     * @return nothing but wait the and of action to continue to the next handler
     */
    default Mono<Void> after(ScrapResult result) {
        return Mono.empty().then();
    }

    /**
     * This was run after the event even if nothing as changes
     */
    default void onTerminate() {
    }

    /**
     * Allow to know what king of event this handler expects. The handler can be triggered on multiple event types.
     * <p>
     * Available event types:
     * <ul>
     *     <li><b>FEED_SCRAPING</b>: The feed list scraping for new entries</li>
     * </ul>
     *
     * @return A set of event types
     */
    Set<String> eventTypes();
}
