package fr.ght1pc9kc.baywatch.scraper.api;

import fr.ght1pc9kc.baywatch.scraper.api.model.ScrapingError;
import fr.ght1pc9kc.entity.api.Entity;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collection;
import java.util.logging.Level;

public interface ScrapingErrorsService {
    Flux<Entity<ScrapingError>> persist(Collection<Entity<ScrapingError>> errors);

    Flux<Entity<ScrapingError>> list(Collection<String> feedsIds);

    Mono<Void> purge(Collection<String> notInFeedsIds);

    Level level(ScrapingError error);

    static String filterMessage(int httpStatusCode) {
        return switch (httpStatusCode) {
            case 200, 599 -> "Error on parsing feed flux. Will be fixed soon.";
            case 403 -> "Feed expect credentials to be read";
            case 404 -> "Feed not found.";
            case 406 -> "Feed format unknown and not supported.";
            case 410 -> "Feed is gone for ever, you can remove it !";
            case 500 -> "Feed unavailable";
            case 521 -> "Feed server is done";
            default -> "Unknown error message for code " + httpStatusCode;
        };
    }
}
