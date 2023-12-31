package fr.ght1pc9kc.baywatch.scraper.api;

import fr.ght1pc9kc.baywatch.techwatch.api.model.RawNews;
import reactor.core.publisher.Mono;

public interface NewsFilter {
    Mono<RawNews> filter(RawNews news);
}
