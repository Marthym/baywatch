package fr.ght1pc9kc.baywatch.domain.scrapper.actions;

import fr.ght1pc9kc.baywatch.api.FeedService;
import fr.ght1pc9kc.baywatch.api.NewsService;
import fr.ght1pc9kc.baywatch.api.model.News;
import fr.ght1pc9kc.baywatch.api.model.request.PageRequest;
import fr.ght1pc9kc.baywatch.api.model.request.filter.Criteria;
import fr.ght1pc9kc.baywatch.api.scrapper.ScrappingHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static fr.ght1pc9kc.baywatch.api.model.EntitiesProperties.FEED_ID;

@Slf4j
@RequiredArgsConstructor
public class DeleteOrphanFeedHandler implements ScrappingHandler {
    private final FeedService feedService;
    private final NewsService newsService;

    @Override
    public Mono<Void> before() {

        //FIXME: A terminer
        return feedService.list(PageRequest.all(Criteria.property(FEED_ID).isNull()))
                .flatMap(feed -> newsService.list(PageRequest.all(Criteria.property(FEED_ID).eq(feed.getId()))))
                .groupBy(News::isShared)
                .flatMap(groups ->
                        groups.map(News::getId)
                                .buffer(500)
                                .flatMap(news -> ((groups.key()) ? newsService.orphanize(news).then() : newsService.delete(news).then())
                                        .thenMany(Flux.fromIterable(news)))
                )
                .buffer(500)
                .flatMap(feedService::delete)
                .reduce(0, Integer::sum)
                .map(count -> {
                    log.debug("{} orphan Feed(s) deleted.", count);
                    return count;
                }).then();
    }

}
