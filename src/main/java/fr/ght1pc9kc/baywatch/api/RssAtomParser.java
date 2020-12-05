package fr.ght1pc9kc.baywatch.api;

import fr.ght1pc9kc.baywatch.api.model.Feed;
import fr.ght1pc9kc.baywatch.api.model.News;
import reactor.core.publisher.Flux;

import java.io.InputStream;

public interface RssAtomParser {
    Flux<News> parse(Feed feed, InputStream is);
}
