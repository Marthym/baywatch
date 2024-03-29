package fr.ght1pc9kc.baywatch.scraper.api;

import fr.ght1pc9kc.baywatch.scraper.api.model.AtomFeed;
import fr.ght1pc9kc.baywatch.scraper.domain.model.ScrapedFeed;
import fr.ght1pc9kc.baywatch.techwatch.api.model.RawNews;
import reactor.core.publisher.Mono;

import javax.xml.stream.events.XMLEvent;
import java.util.List;
import java.util.function.Predicate;

public interface RssAtomParser {
    /**
     * Give the predicate for skiping the XML lines until the first item or entry element.
     *
     * @return The {@link Predicate} returning {@code true} if {@link XMLEvent} is the first item to parse
     */
    Predicate<XMLEvent> firstItemEvent();

    /**
     * This predicated allow to bufferize {@link XMLEvent} form the opening tag to the closing tag
     *
     * @return The {@link Predicate} returning {@code true} if {@link XMLEvent} is the end of item.
     */
    Predicate<XMLEvent> itemEndEvent();

    /**
     * Parsing a homogenous list of tags composing a feed entity
     *
     * @param events The {@link XMLEvent} composing a feed entity
     * @return The {@link RawNews} representing the feed entry
     */
    AtomFeed readFeedProperties(List<XMLEvent> events);

    /**
     * Parsing a homogenous list of tags composing an item ou entry
     *
     * @param events The {@link XMLEvent} composing a feed entry
     * @param feed   The parent feed. Used for absolutize link if necessary
     * @return The {@link RawNews} representing the feed entry
     */
    Mono<RawNews> readEntryEvents(List<XMLEvent> events, ScrapedFeed feed);


}
