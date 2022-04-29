package fr.ght1pc9kc.baywatch.scrapper.domain;

import com.machinezoo.noexception.Exceptions;
import fr.ght1pc9kc.baywatch.scrapper.api.FeedParserPlugin;
import fr.ght1pc9kc.baywatch.scrapper.api.RssAtomParser;
import fr.ght1pc9kc.baywatch.techwatch.api.model.Feed;
import fr.ght1pc9kc.baywatch.techwatch.api.model.News;
import fr.ght1pc9kc.baywatch.techwatch.api.model.RawNews;
import fr.ght1pc9kc.baywatch.techwatch.api.model.State;
import lombok.extern.slf4j.Slf4j;
import org.owasp.html.PolicyFactory;
import org.owasp.html.Sanitizers;
import reactor.core.publisher.Mono;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import java.io.StringWriter;
import java.net.URI;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Slf4j
public final class RssAtomParserImpl implements RssAtomParser {
    private static final Set<String> ALLOWED_PROTOCOL = Set.of("http", "https");
    private static final String ITEM = "item";
    private static final String ENTRY = "entry";
    private static final String TITLE = "title";
    private static final String DESCRIPTION = "description";
    private static final String CONTENT = "content";
    private static final String LINK = "link";
    private static final String PUB_DATE = "pubDate";
    private static final String UPDATED = "updated";

    private static final QName HREF = new QName("href");
    private static final PolicyFactory HTML_POLICY = Sanitizers.FORMATTING;
    private final Map<String, FeedParserPlugin> plugins;

    public RssAtomParserImpl(List<FeedParserPlugin> plugins) {
        this.plugins = plugins.stream()
                .collect(Collectors.toUnmodifiableMap(FeedParserPlugin::pluginForDomain, Function.identity()));
    }

    @Override
    public Predicate<XMLEvent> firstItemEvent() {
        return e -> e.isStartElement() && (
                ITEM.equals(e.asStartElement().getName().getLocalPart())
                        || ENTRY.equals(e.asStartElement().getName().getLocalPart())
        );
    }

    @Override
    public Predicate<XMLEvent> itemEndEvent() {
        return e -> e.isEndElement() && (
                ITEM.equals(e.asEndElement().getName().getLocalPart())
                        || ENTRY.equals(e.asEndElement().getName().getLocalPart())
        );
    }

    @Override
    public Mono<News> readEntryEvents(List<XMLEvent> events, Feed feed) {
        FeedParserPlugin defaultPlugin = plugins.getOrDefault("*", FeedParserPlugin.NO_PLUGIN);
        FeedParserPlugin plugin = plugins.getOrDefault(feed.getUrl().getHost(), defaultPlugin);

        RawNews.RawNewsBuilder bldr = null;
        for (int i = 0; i < events.size(); i++) {
            final XMLEvent nextEvent = events.get(i);
            if (nextEvent.isStartElement()) {
                final StartElement startElement = nextEvent.asStartElement();
                if (!startElement.getName().getPrefix().isBlank()) {
                    continue;
                }

                bldr = switch (startElement.getName().getLocalPart()) {
                    case ENTRY, ITEM -> onItemEntry(plugin);
                    case TITLE -> onTitle(bldr, plugin, events, i);
                    case CONTENT, DESCRIPTION -> onContentDescription(bldr, plugin, events, i);
                    case LINK -> onLink(bldr, plugin, feed, events, i);
                    case UPDATED -> onUpdated(bldr, plugin, events, i);
                    case PUB_DATE -> onPublicationDate(bldr, plugin, events, i);
                    default -> bldr;
                };
            }
        }

        if (bldr == null) {
            return Mono.empty();
        }

        RawNews rawNews = plugin.handleEndEvent(bldr);
        if (rawNews.getLink().getScheme() == null
                || !ALLOWED_PROTOCOL.contains(rawNews.getLink().getScheme())) {
            log.warn("Illegal URL detected : {} in feed :{}", rawNews.getLink(), feed.getName());
            return Mono.empty();
        }
        RawNews raw = rawNews
                .withTitle(HTML_POLICY.sanitize(rawNews.title))
                .withDescription(HTML_POLICY.sanitize(rawNews.description));

        return Mono.just(News.builder()
                .raw(raw)
                .feeds(Set.of(feed.getId()))
                .state(State.NONE)
                .build());
    }

    private RawNews.RawNewsBuilder onItemEntry(FeedParserPlugin plugin) {
        log.trace("start parsing entry");
        return plugin.handleItemEvent();
    }

    private RawNews.RawNewsBuilder onTitle(RawNews.RawNewsBuilder bldr, FeedParserPlugin plugin,
                                           List<XMLEvent> events, int idx) {
        if (bldr == null) {
            return null;
        }
        String title = readElementText(events, idx);
        return plugin.handleTitleEvent(bldr, title);
    }

    private RawNews.RawNewsBuilder onContentDescription(RawNews.RawNewsBuilder bldr, FeedParserPlugin plugin,
                                                        List<XMLEvent> events, int idx) {
        if (bldr == null) {
            return null;
        }
        return plugin.handleDescriptionEvent(bldr, readElementText(events, idx));
    }

    private RawNews.RawNewsBuilder onLink(RawNews.RawNewsBuilder bldr, FeedParserPlugin plugin, Feed feed,
                                          List<XMLEvent> events, int idx) {
        if (bldr == null) {
            return null;
        }
        final int nextIdx = idx + 1;
        StartElement startElement = events.get(idx).asStartElement();
        String href = Optional.ofNullable(startElement.getAttributeByName(HREF))
                .map(Attribute::getValue)
                .orElseGet(Exceptions.wrap().supplier(() -> events.get(nextIdx).asCharacters().getData()));
        URI link = URI.create(href.trim());
        if (!link.isAbsolute()) {
            link = feed.getUrl().resolve(link);
        }
        return plugin.handleLinkEvent(bldr, link);
    }

    private RawNews.RawNewsBuilder onUpdated(RawNews.RawNewsBuilder bldr, FeedParserPlugin plugin,
                                             List<XMLEvent> events, int idx) {
        if (bldr == null) {
            return null;
        }
        String updated = events.get(idx + 1).asCharacters().getData();
        Instant updatedAt = DateTimeFormatter.ISO_OFFSET_DATE_TIME.parse(updated, Instant::from);
        return plugin.handlePublicationEvent(bldr, updatedAt);
    }

    private RawNews.RawNewsBuilder onPublicationDate(RawNews.RawNewsBuilder bldr, FeedParserPlugin plugin,
                                                     List<XMLEvent> events, int idx) {
        if (bldr == null) {
            return null;
        }
        String pubDate = events.get(idx + 1).asCharacters().getData();
        Instant datetime = Exceptions.silence().get(() -> DateTimeFormatter.RFC_1123_DATE_TIME.parse(pubDate, Instant::from))
                .orElseGet(() -> DateTimeFormatter.ISO_OFFSET_DATE_TIME.parse(pubDate, Instant::from));
        return plugin.handlePublicationEvent(bldr, datetime);
    }

    private String readElementText(List<XMLEvent> events, int idx) {
        StringWriter buf = new StringWriter(1024);
        for (int i = idx + 1; i < events.size() - 1; i++) {
            XMLEvent textEvent = events.get(i);
            if (textEvent.isCharacters()) {
                try {
                    textEvent.writeAsEncodedUnicode(buf);
                } catch (XMLStreamException e) {
                    log.debug("Fail to write title buffer at index " + i, e);
                }
            } else {
                break;
            }
        }
        return buf.getBuffer().toString().trim();
    }
}
