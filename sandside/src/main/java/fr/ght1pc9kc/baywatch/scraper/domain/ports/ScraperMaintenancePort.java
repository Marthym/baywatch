package fr.ght1pc9kc.baywatch.scraper.domain.ports;

import fr.ght1pc9kc.baywatch.common.api.model.FeedMeta;
import fr.ght1pc9kc.baywatch.scraper.api.model.AtomFeed;
import fr.ght1pc9kc.baywatch.scraper.domain.model.ScrapedFeed;
import fr.ght1pc9kc.baywatch.techwatch.api.model.News;
import fr.ght1pc9kc.entity.api.Entity;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collection;
import java.util.Map;

public interface ScraperMaintenancePort {
    Flux<String> listAllNewsId();

    Flux<ScrapedFeed> feedList();

    Mono<Integer> newsLoad(Collection<News> toLoad);

    /**
     * <p>Update a feed with available data.</p>
     * <p>Only Description, tags, last publication date, title</p>
     *
     * @param id          The feed ID to update
     * @param updatedFeed The updated feed information to persist
     * @return The updated {@link AtomFeed}
     */
    Mono<AtomFeed> feedUpdate(String id, AtomFeed updatedFeed);

    Mono<AtomFeed> feedUpdateMetas(Entity<AtomFeed> toUpdate);
}
