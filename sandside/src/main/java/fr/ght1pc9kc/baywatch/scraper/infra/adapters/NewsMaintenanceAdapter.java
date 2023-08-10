package fr.ght1pc9kc.baywatch.scraper.infra.adapters;

import fr.ght1pc9kc.baywatch.scraper.domain.model.ScrapedFeed;
import fr.ght1pc9kc.baywatch.scraper.domain.ports.NewsMaintenancePort;
import fr.ght1pc9kc.baywatch.techwatch.api.SystemMaintenanceService;
import fr.ght1pc9kc.baywatch.techwatch.api.model.News;
import fr.ght1pc9kc.juery.api.PageRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collection;

@Component
@RequiredArgsConstructor
public class NewsMaintenanceAdapter implements NewsMaintenancePort {
    private final SystemMaintenanceService systemMaintenanceService;

    @Override
    public Flux<String> listAllNewsId() {
        return systemMaintenanceService.newsIdList(PageRequest.all());
    }

    @Override
    public Flux<ScrapedFeed> feedList() {
        return systemMaintenanceService.feedList(PageRequest.all())
                .map(f -> new ScrapedFeed(f.id(), f.url()));
    }

    @Override
    public Mono<Integer> newsLoad(Collection<News> toLoad) {
        return systemMaintenanceService.newsLoad(toLoad);
    }
}
