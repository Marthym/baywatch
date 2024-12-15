package fr.ght1pc9kc.baywatch.scraper.infra.adapters.services;

import fr.ght1pc9kc.baywatch.scraper.api.model.AtomFeed;
import fr.ght1pc9kc.baywatch.scraper.domain.model.ScrapedFeed;
import fr.ght1pc9kc.baywatch.scraper.domain.ports.ScraperMaintenancePort;
import fr.ght1pc9kc.baywatch.techwatch.api.SystemMaintenanceService;
import fr.ght1pc9kc.baywatch.techwatch.api.model.News;
import fr.ght1pc9kc.baywatch.techwatch.api.model.WebFeed;
import fr.ght1pc9kc.baywatch.techwatch.infra.config.TechwatchMapper;
import fr.ght1pc9kc.entity.api.Entity;
import fr.ght1pc9kc.juery.api.PageRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.Collection;
import java.util.List;

import static fr.ght1pc9kc.baywatch.common.api.model.FeedMeta.ETag;
import static fr.ght1pc9kc.baywatch.common.api.model.FeedMeta.updated;

@Component
@RequiredArgsConstructor
public class ScraperMaintenanceAdapter implements ScraperMaintenancePort {
    private final SystemMaintenanceService systemMaintenanceService;
    private final TechwatchMapper techwatchMapper;

    @Override
    public Flux<String> listAllNewsId() {
        return systemMaintenanceService.newsIdList(PageRequest.all());
    }

    @Override
    public Flux<ScrapedFeed> feedList() {
        return systemMaintenanceService.feedList(PageRequest.all())
                .map(f -> new ScrapedFeed(f.id(), f.self().location(),
                        f.meta(updated, Instant.class).orElse(Instant.EPOCH), f.meta(ETag).orElse(null)));
    }

    @Override
    public Mono<Integer> newsLoad(Collection<News> toLoad) {
        return systemMaintenanceService.newsLoad(toLoad);
    }

    @Override
    public Mono<Void> feedsUpdate(Collection<Entity<AtomFeed>> toUpdate) {
        List<Entity<WebFeed>> webFeeds = toUpdate.stream()
                .map(eaf -> eaf.convert(techwatchMapper::getFeedFromAtom))
                .toList();
        return systemMaintenanceService.feedsUpdate(webFeeds);
    }
}
