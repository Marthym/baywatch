package fr.ght1pc9kc.baywatch.scrapper.api;

import fr.ght1pc9kc.baywatch.techwatch.api.model.Feed;
import fr.ght1pc9kc.baywatch.techwatch.api.model.News;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.xml.stream.events.XMLEvent;
import java.io.InputStream;
import java.util.List;

public interface RssAtomParser {
    Flux<News> parse(Feed feed, InputStream is);

    default Mono<News> readEntryEvents(List<XMLEvent> events, Feed feed) {
        return Mono.empty();
    }
}
