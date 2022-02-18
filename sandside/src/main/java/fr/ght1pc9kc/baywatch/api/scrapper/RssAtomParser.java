package fr.ght1pc9kc.baywatch.api.scrapper;

import fr.ght1pc9kc.baywatch.api.techwatch.model.Feed;
import fr.ght1pc9kc.baywatch.api.techwatch.model.News;
import reactor.core.publisher.Flux;

import java.io.InputStream;

public interface RssAtomParser {
    Flux<News> parse(Feed feed, InputStream is);
}
