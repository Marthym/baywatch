package fr.ght1pc9kc.baywatch.scrapper.api;

import fr.ght1pc9kc.baywatch.techwatch.api.model.Feed;
import fr.ght1pc9kc.baywatch.techwatch.api.model.News;
import reactor.core.publisher.Flux;

import java.io.InputStream;

public interface RssAtomParser {
    Flux<News> parse(Feed feed, InputStream is);
}
