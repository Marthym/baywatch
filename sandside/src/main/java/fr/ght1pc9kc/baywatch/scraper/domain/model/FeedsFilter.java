package fr.ght1pc9kc.baywatch.scraper.domain.model;

import fr.ght1pc9kc.baywatch.scraper.api.model.AtomFeed;
import org.jetbrains.annotations.NotNull;
import reactor.core.publisher.Mono;

public interface FeedsFilter {
    Mono<AtomFeed> filter(@NotNull AtomFeed feed);
}
