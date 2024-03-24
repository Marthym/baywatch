package fr.ght1pc9kc.baywatch.scraper.domain.ports;

import fr.ght1pc9kc.baywatch.scraper.api.model.AtomFeed;
import fr.ght1pc9kc.baywatch.scraper.domain.model.ScrapedFeed;
import fr.ght1pc9kc.baywatch.techwatch.api.model.News;
import fr.ght1pc9kc.entity.api.Entity;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collection;

public interface ScraperMaintenancePort {
    Flux<String> listAllNewsId();

    Flux<ScrapedFeed> feedList();

    Mono<Integer> newsLoad(Collection<News> toLoad);

    /**
     * <p>Update a feed with available data.</p>
     * <p>Only Description, tags, last publication date, title</p>
     *
     * @param toUpdate The updated feed information to persist
     * @return The updated {@link AtomFeed}
     */
    Mono<Void> feedsUpdate(Collection<Entity<AtomFeed>> toUpdate);
}
