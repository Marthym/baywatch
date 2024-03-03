package fr.ght1pc9kc.baywatch.scraper.infra.adapters;

import fr.ght1pc9kc.baywatch.scraper.api.model.AtomFeed;
import fr.ght1pc9kc.baywatch.scraper.domain.model.ScrapedFeed;
import fr.ght1pc9kc.baywatch.scraper.domain.ports.ScraperMaintenancePort;
import fr.ght1pc9kc.baywatch.techwatch.api.SystemMaintenanceService;
import fr.ght1pc9kc.baywatch.techwatch.api.model.News;
import fr.ght1pc9kc.baywatch.techwatch.api.model.WebFeed;
import fr.ght1pc9kc.juery.api.PageRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.time.Instant;
import java.util.Collection;
import java.util.Optional;
import java.util.Set;

import static fr.ght1pc9kc.baywatch.common.api.model.FeedMeta.ETag;
import static fr.ght1pc9kc.baywatch.common.api.model.FeedMeta.updated;

@Component
@RequiredArgsConstructor
public class ScraperMaintenanceAdapter implements ScraperMaintenancePort {
    private final SystemMaintenanceService systemMaintenanceService;

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
    public Mono<AtomFeed> feedUpdate(String id, AtomFeed updatedFeed) {
        return Mono.fromCallable(() -> WebFeed.builder()
                        .name(updatedFeed.title())
                        .reference(Optional.ofNullable(updatedFeed.id()).orElse(id))
                        .location(Optional.ofNullable(updatedFeed.link()).orElse(URI.create("#")))
                        .description(updatedFeed.description())
                        .tags(Set.of())
                        .build()).flatMap(wf -> systemMaintenanceService.feedUpdate(id, wf))
                .map(f -> AtomFeed.builder()
                        .id(f.self().reference())
                        .title(f.self().name())
                        .link(f.self().location())
                        .description(f.self().description())
                        .build());
    }
}
