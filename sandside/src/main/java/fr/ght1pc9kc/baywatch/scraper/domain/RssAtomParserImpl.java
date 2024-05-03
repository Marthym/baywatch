package fr.ght1pc9kc.baywatch.scraper.domain;

import com.machinezoo.noexception.Exceptions;
import fr.ght1pc9kc.baywatch.common.domain.Hasher;
import fr.ght1pc9kc.baywatch.scraper.api.RssAtomParser;
import fr.ght1pc9kc.baywatch.scraper.api.model.AtomFeed;
import fr.ght1pc9kc.baywatch.scraper.domain.model.Publishable;
import fr.ght1pc9kc.baywatch.scraper.domain.model.RssNamespaces;
import fr.ght1pc9kc.baywatch.scraper.domain.model.ScrapedFeed;
import fr.ght1pc9kc.baywatch.techwatch.api.model.RawNews;
import lombok.AccessLevel;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.VisibleForTesting;
import reactor.core.publisher.Mono;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.Characters;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import java.io.StringWriter;
import java.net.URI;
import java.text.ParsePosition;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;

/**
 * This class parses and renders data as found in news feeds. Under no circumstances is it responsible for sanitizing the data read.
 * Depending on their use, it will be necessary to think about sanitizing them before displaying them.
 */
@Slf4j
public final class RssAtomParserImpl implements RssAtomParser {
    private static final Set<String> ALLOWED_PROTOCOL = Set.of("http", "https");
    private static final String CHANNEL = "channel";
    private static final String CONTENT = "content";
    private static final String DC_DATE = "dc:date";
    private static final String DESCRIPTION = "description";
    private static final String EMAIL = "email";
    private static final String ENTRY = "entry";
    private static final String FEED = "feed";
    private static final String ITEM = "item";
    private static final String LAST_BUILD_DATE = "lastBuildDate";
    private static final String LINK = "link";
    private static final String MANAGING_EDITOR = "managingEditor";
    private static final String NAME = "name";
    private static final String PUB_DATE = "pubDate";
    private static final String SUBTITLE = "subtitle";
    private static final String TITLE = "title";
    private static final String UPDATED = "updated";

    private static final QName HREF = new QName("href");
    private static final QName TYPE = new QName("type");
    private static final QName REL = new QName("rel");
    private static final String SELF = "self";

    private static final String MIME_TEXT_HTML = "text/html";

    private static final DateTimeFormatter NON_STANDARD_DATETIME = DateTimeFormatter.ofPattern("uuuu-MM-dd HH:mm:ss")
            .withZone(ZoneOffset.UTC);
    private static final DateTimeFormatter UBER_DATETIME = DateTimeFormatter.ofPattern("EEE MMM dd yyyy HH:mm:ss zzzXXXX")
            .withZone(ZoneOffset.UTC).withLocale(Locale.US);

    @Setter(value = AccessLevel.PACKAGE, onMethod = @__({@VisibleForTesting}))
    private Clock clock = Clock.systemUTC();

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
    public AtomFeed readFeedProperties(List<XMLEvent> events) {
        String title = null;
        String description = null;
        String author = null;
        URI link = null;
        Instant updated = null;

        int deepLevel = -1;

        for (int i = 0; i < events.size(); i++) {
            final XMLEvent nextEvent = events.get(i);
            if (nextEvent.isStartElement()) {
                final StartElement startElement = nextEvent.asStartElement();

                final int idx = i;

                switch (startElement.getName().getLocalPart()) {
                    case CHANNEL, FEED -> deepLevel = -1;
                    case TITLE -> title = readElementText(events, idx);
                    case DESCRIPTION, SUBTITLE -> {
                        String tmpDescr = readElementText(events, idx);
                        if (!StringUtils.isBlank(tmpDescr))
                            description = tmpDescr;
                    }
                    case LINK -> link = onFeedLink(link, deepLevel, events, idx);
                    case NAME, MANAGING_EDITOR -> author = Optional.ofNullable(author)
                            .map(a -> readElementText(events, idx) + " " + a)
                            .orElse(readElementText(events, idx));
                    case EMAIL -> author = Optional.ofNullable(author)
                            .map(a -> a + " <" + readElementText(events, idx) + ">")
                            .orElse("<" + readElementText(events, idx) + ">");
                    case UPDATED -> updated = onUpdated(pubDate -> pubDate, events, i);
                    case PUB_DATE, LAST_BUILD_DATE -> updated = onPublicationDate(pubDate -> pubDate, events, i);
                    default -> {/* ignore */}
                }
                deepLevel++;

            } else if (nextEvent.isEndElement()) {
                deepLevel--;
            }
            if (this.firstItemEvent().test(nextEvent)) {
                break;
            }
        }

        if (updated == null) {
            updated = clock.instant();
        }
        return new AtomFeed(Optional.ofNullable(link).map(Hasher::identify).orElse(null),
                title, description, author, link, updated);
    }

    private static URI onFeedLink(URI old, int deepLevel, List<XMLEvent> events, int idx) {
        if (deepLevel == 0) {
            StartElement startElement = events.get(idx).asStartElement();
            String relAttr = Optional.ofNullable(startElement.getAttributeByName(REL))
                    .map(Attribute::getValue).orElse(null);
            if ((old == null && relAttr == null) || SELF.equals(relAttr)) {
                return Optional.ofNullable(startElement.getAttributeByName(HREF))
                        .map(Attribute::getValue)
                        .map(URI::create)
                        .orElseGet(Exceptions.wrap().supplier(() -> URI.create(readElementText(events, idx))));
            }
        }
        return old;
    }

    @Override
    public Mono<RawNews> readEntryEvents(List<XMLEvent> events, ScrapedFeed feed) {
        RawNews.RawNewsBuilder bldr = null;
        for (int i = 0; i < events.size(); i++) {
            final XMLEvent nextEvent = events.get(i);
            if (nextEvent.isStartElement()) {
                final StartElement startElement = nextEvent.asStartElement();
                Optional<RssNamespaces> fieldNamespace = RssNamespaces.fromURI(startElement.getName().getNamespaceURI());
                if (!startElement.getName().getPrefix().isEmpty() && fieldNamespace.isEmpty()) {
                    continue;
                }

                String fieldName = fieldNamespace
                        .map(ns -> startElement.getName().getPrefix() + ':' + startElement.getName().getLocalPart())
                        .orElse(startElement.getName().getLocalPart());

                bldr = switch (fieldName) {
                    case ENTRY, ITEM -> onItemEntry();
                    case TITLE -> onTitle(bldr, events, i);
                    case CONTENT, DESCRIPTION -> onContentDescription(bldr, events, i);
                    case LINK -> onLink(bldr, feed, events, i);
                    case DC_DATE, UPDATED -> onUpdated(bldr, events, i);
                    case PUB_DATE -> onPublicationDate(bldr, events, i);
                    default -> bldr;
                };
            }
        }

        if (bldr == null) {
            return Mono.empty();
        }

        RawNews rawNews = bldr.build();
        if (rawNews.link().getScheme() == null
                || !ALLOWED_PROTOCOL.contains(rawNews.link().getScheme())) {
            log.warn("Illegal URL detected : {} in feed : {}", rawNews.link(), feed.id().substring(0, 10));
            return Mono.empty();
        }

        return Mono.just(rawNews
                .withTitle(rawNews.title())
                .withDescription(rawNews.description()));
    }

    private RawNews.RawNewsBuilder onItemEntry() {
        log.trace("start parsing entry");
        return RawNews.builder()
                .publication(clock.instant()); // Default value
    }

    private RawNews.RawNewsBuilder onTitle(RawNews.RawNewsBuilder bldr,
                                           List<XMLEvent> events, int idx) {
        RawNews.RawNewsBuilder result = null;
        if (bldr != null) {
            String title = readElementText(events, idx);
            result = bldr.title(title);
        }
        return result;
    }

    private RawNews.RawNewsBuilder onContentDescription(RawNews.RawNewsBuilder bldr,
                                                        List<XMLEvent> events, int idx) {
        RawNews.RawNewsBuilder result;
        if (bldr == null) {
            result = null;
        } else {
            String content = readElementText(events, idx);
            if (content.isBlank()) {
                result = bldr;
            } else {
                result = bldr.description(content);
            }
        }
        return result;
    }

    private RawNews.RawNewsBuilder onLink(RawNews.RawNewsBuilder bldr, ScrapedFeed feed,
                                          List<XMLEvent> events, int idx) {
        if (bldr == null) {
            return null;
        }
        StartElement startElement = events.get(idx).asStartElement();
        String linkType = Optional.ofNullable(startElement.getAttributeByName(TYPE))
                .map(Attribute::getValue)
                .orElse(MIME_TEXT_HTML);
        if (!MIME_TEXT_HTML.equals(linkType)) {
            log.atTrace().addArgument(linkType).addArgument(feed.shortId())
                    .log("Ignore link type {} for feed {}");
            return bldr;
        }
        String href = Optional.ofNullable(startElement.getAttributeByName(HREF))
                .map(Attribute::getValue)
                .orElseGet(Exceptions.wrap().supplier(() -> readElementText(events, idx)));
        URI link = URI.create(href.trim());
        if (!link.isAbsolute()) {
            link = feed.link().resolve(link);
        }
        return bldr.id(Hasher.identify(link)).link(link);
    }

    private <T> T onUpdated(Publishable<T> bldr,
                            List<XMLEvent> events, int idx) {
        T result = null;
        if (bldr != null) {
            String updated = readElementText(events, idx);
            Instant updatedAt = DateTimeFormatter.ISO_OFFSET_DATE_TIME.parse(updated, Instant::from);
            result = bldr.publication(updatedAt);
        }
        return result;
    }

    private <T> T onPublicationDate(Publishable<T> bldr,
                                    List<XMLEvent> events, int idx) {
        T result = null;
        if (bldr != null) {
            String pubDate = readElementText(events, idx);
            try {

                Instant datetime = Exceptions.silence().get(() -> DateTimeFormatter.RFC_1123_DATE_TIME.parse(pubDate, Instant::from))
                        .orElseGet(Exceptions.silence().supplier(() -> DateTimeFormatter.ISO_OFFSET_DATE_TIME.parse(pubDate, Instant::from))
                                .orElseGet(Exceptions.silence().supplier(() -> Instant.from(UBER_DATETIME.parse(pubDate, new ParsePosition(0))))
                                        .orElseGet(() -> NON_STANDARD_DATETIME.parse(pubDate, Instant::from))));
                result = bldr.publication(datetime);
            } catch (Exception e) {
                log.atDebug().addArgument(pubDate)
                        .addArgument(e.getLocalizedMessage())
                        .log("Unable to parse \"{}\" : {}");
            }
        }
        return result;
    }

    private static String readElementText(List<XMLEvent> events, int idx) {
        StringWriter buf = new StringWriter(1024);
        for (int i = idx + 1; i < events.size() - 1; i++) {
            XMLEvent textEvent = events.get(i);
            if (textEvent.isCharacters()) {
                try {
                    Characters charEvent = (Characters) textEvent;
                    if (charEvent.isCData()) {
                        buf.write(charEvent.getData());
                    } else {
                        textEvent.writeAsEncodedUnicode(buf);
                    }
                } catch (XMLStreamException e) {
                    log.debug("Fail to write title buffer at index " + i, e);
                }
            } else {
                break;
            }
        }

        return StringUtils.normalizeSpace(buf.getBuffer().toString());
    }

}
