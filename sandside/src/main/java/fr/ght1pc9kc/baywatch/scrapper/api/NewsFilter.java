package fr.ght1pc9kc.baywatch.scrapper.api;

import fr.ght1pc9kc.baywatch.techwatch.api.model.RawNews;
import reactor.core.publisher.Mono;

public interface NewsFilter {
    NewsFilterStep getStep();

    Mono<RawNews> filter(RawNews news);
}
