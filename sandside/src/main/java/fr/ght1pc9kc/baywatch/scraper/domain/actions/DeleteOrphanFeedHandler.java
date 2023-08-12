package fr.ght1pc9kc.baywatch.scraper.domain.actions;

import fr.ght1pc9kc.baywatch.common.api.ScrapingEventHandler;
import fr.ght1pc9kc.baywatch.techwatch.api.SystemMaintenanceService;
import fr.ght1pc9kc.baywatch.techwatch.api.model.Feed;
import fr.ght1pc9kc.baywatch.techwatch.api.model.News;
import fr.ght1pc9kc.juery.api.Criteria;
import fr.ght1pc9kc.juery.api.PageRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

import java.util.Set;

import static fr.ght1pc9kc.baywatch.common.api.model.EntitiesProperties.COUNT;
import static fr.ght1pc9kc.baywatch.common.api.model.EntitiesProperties.FEED_ID;

/**
 * Delete the orphan {@link Feed}. Orphan was the Feeds followed by nobody.
 * <p>
 * If the Feed to delete have {@link News} the News are deleted
 */
@Slf4j
@RequiredArgsConstructor
public class DeleteOrphanFeedHandler implements ScrapingEventHandler {

    private static final int BATCH_BUFFER_SIZE = 500;

    private final SystemMaintenanceService maintenanceService;

    @Override
    public Mono<Void> before() {
        log.debug("DeleteOrphanFeedHandler Start ...");
        Sinks.Many<String> newsSink = Sinks.many().multicast().onBackpressureBuffer();
        Sinks.Many<String> feeds = Sinks.many().multicast().onBackpressureBuffer();

        Mono<Void> deleted = newsSink.asFlux()
                .buffer(BATCH_BUFFER_SIZE)
                .flatMap(maintenanceService::newsDelete)
                .reduce(0, Integer::sum)
                .doOnSuccess(count -> log.debug("{} dependent News(s) deleted.", count))
                .then();

        Mono<Void> deletedFeeds = feeds.asFlux()
                .buffer(BATCH_BUFFER_SIZE)
                .flatMap(maintenanceService::feedDelete)
                .reduce(0, Integer::sum)
                .doOnSuccess(count -> log.debug("{} Feed(s) deleted.", count))
                .then();

        return maintenanceService.feedList(PageRequest.all(Criteria.property(COUNT).eq(0)))
                .doOnNext(feed -> feeds.tryEmitNext(feed.id()))
                .doOnComplete(feeds::tryEmitComplete)
                .flatMap(feed -> maintenanceService.newsList(PageRequest.all(Criteria.property(FEED_ID).eq(feed.id()))))
                .doOnComplete(newsSink::tryEmitComplete)
                .doOnNext(news -> newsSink.tryEmitNext(news.id()))
                .then(deleted)
                .then(deletedFeeds)
                .doOnTerminate(() -> log.debug("DeleteOrphanFeedHandler terminated."));
    }

    @Override
    public Set<String> eventTypes() {
        return Set.of("FEED_SCRAPING");
    }

}
