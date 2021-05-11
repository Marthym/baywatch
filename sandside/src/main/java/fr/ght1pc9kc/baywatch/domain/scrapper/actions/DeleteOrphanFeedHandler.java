package fr.ght1pc9kc.baywatch.domain.scrapper.actions;

import fr.ght1pc9kc.baywatch.api.NewsService;
import fr.ght1pc9kc.baywatch.api.admin.FeedAdminService;
import fr.ght1pc9kc.baywatch.api.scrapper.ScrappingHandler;
import fr.ght1pc9kc.juery.api.Criteria;
import fr.ght1pc9kc.juery.api.PageRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

import java.util.Collection;

import static fr.ght1pc9kc.baywatch.api.model.EntitiesProperties.FEED_ID;

/**
 * Delete the orphan {@link fr.ght1pc9kc.baywatch.api.model.Feed}. Orphan was the Feeds followed by nobody.
 * <p>
 * If the Feed to delete have {@link fr.ght1pc9kc.baywatch.api.model.News} the News are deleted or, if the News is
 * shared, it was {@link NewsService#orphanize(Collection)}.
 */
@Slf4j
@RequiredArgsConstructor
public class DeleteOrphanFeedHandler implements ScrappingHandler {

    private static final int BATCH_BUFFER_SIZE = 500;

    private final FeedAdminService feedAdminService;
    private final NewsService newsService;

    @Override
    public Mono<Void> before() {
        log.debug("DeleteOrphanFeedHandler Start ...");
        Sinks.Many<String> shared = Sinks.many().multicast().onBackpressureBuffer();
        Sinks.Many<String> unshared = Sinks.many().multicast().onBackpressureBuffer();
        Sinks.Many<String> feeds = Sinks.many().multicast().onBackpressureBuffer();

        Mono<Integer> orphanized = shared.asFlux()
                .buffer(BATCH_BUFFER_SIZE)
                .flatMap(newsService::orphanize)
                .reduce(0, Integer::sum);

        Mono<Integer> deleted = unshared.asFlux()
                .buffer(BATCH_BUFFER_SIZE)
                .flatMap(newsService::delete)
                .reduce(0, Integer::sum);

        Mono<Void> finalizeFlux = Mono.zip(orphanized, deleted)
                .doOnSuccess(count -> log.debug("{} dependent News(s) unlinked, {} deleted.", count.getT1(), count.getT2()))
                .then();

        Mono<Void> deletedFeeds = feeds.asFlux()
                .buffer(BATCH_BUFFER_SIZE)
                .flatMap(feedAdminService::delete)
                .reduce(0, Integer::sum)
                .doOnSuccess(count -> log.debug("{} Feed(s) deleted.", count))
                .then();

        return feedAdminService.list(PageRequest.all(Criteria.property(FEED_ID).isNull()))
                .doOnNext(feed -> feeds.tryEmitNext(feed.getId()))
                .doOnComplete(feeds::tryEmitComplete)
                .flatMap(feed -> newsService.list(PageRequest.all(Criteria.property(FEED_ID).eq(feed.getId()))))
                .doOnComplete(() -> {
                    shared.tryEmitComplete();
                    unshared.tryEmitComplete();
                })
                .doOnNext(news -> {
                    if (news.isShared()) {
                        shared.tryEmitNext(news.getId());
                    } else {
                        unshared.tryEmitNext(news.getId());
                    }
                })
                .then(finalizeFlux)
                .then(deletedFeeds)
                .doOnTerminate(() -> log.debug("DeleteOrphanFeedHandler terminated."));
    }

}
