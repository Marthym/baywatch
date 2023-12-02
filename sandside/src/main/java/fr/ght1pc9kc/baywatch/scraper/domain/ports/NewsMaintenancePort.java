package fr.ght1pc9kc.baywatch.scraper.domain.ports;

import fr.ght1pc9kc.baywatch.scraper.domain.model.ScrapedFeed;
import fr.ght1pc9kc.baywatch.techwatch.api.model.News;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collection;

public interface NewsMaintenancePort {
    Flux<String> listAllNewsId();

    Flux<ScrapedFeed> feedList();

    Mono<Integer> newsLoad(Collection<News> toLoad);
}
