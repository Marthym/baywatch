package fr.ght1pc9kc.baywatch.scraper.domain;

import com.machinezoo.noexception.Exceptions;
import fr.ght1pc9kc.baywatch.common.domain.Hasher;
import fr.ght1pc9kc.baywatch.scraper.api.RssAtomParser;
import fr.ght1pc9kc.baywatch.scraper.api.model.AtomFeed;
import fr.ght1pc9kc.baywatch.scraper.domain.model.ScrapedFeed;
import fr.ght1pc9kc.baywatch.techwatch.api.model.RawNews;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.owasp.html.PolicyFactory;
import org.owasp.html.Sanitizers;
import reactor.core.publisher.Mono;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.Characters;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import java.io.StringWriter;
import java.net.URI;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;

@Slf4j
public final class RssAtomParserImpl implements RssAtomParser {
    private static final Set<String> ALLOWED_PROTOCOL = Set.of("http", "https");
    private static final String CHANNEL = "channel";
    private static final String CONTENT = "content";
    private static final String DESCRIPTION = "description";
    private static final String EMAIL = "email";
    private static final String ENTRY = "entry";
    private static final String FEED = "feed";
    private static final String ID = "id";
    private static final String ITEM = "item";
    private static final String LINK = "link";
    private static final String MANAGING_EDITOR = "managingEditor";
    private static final String NAME = "name";
    private static final String PUB_DATE = "pubDate";
    private static final String SUBTITLE = "subtitle";
    private static final String TITLE = "title";
    private static final String UPDATED = "updated";

    private static final QName HREF = new QName("href");
    private static final QName REL = new QName("rel");
    private static final String SELF = "self";
    private static final PolicyFactory HTML_POLICY = Sanitizers.FORMATTING;

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
        String id = null;
        String title = null;
        String description = null;
        String author = null;
        URI link = null;

        int deepLevel = -1;

        for (int i = 0; i < events.size(); i++) {
            final XMLEvent nextEvent = events.get(i);
            if (nextEvent.isStartElement()) {
                final StartElement startElement = nextEvent.asStartElement();

                final int idx = i;

                switch (startElement.getName().getLocalPart()) {
                    case CHANNEL, FEED -> deepLevel = -1;
                    case ID -> id = readElementText(events, idx);
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
                    default -> {/* ignore */}
                }
                deepLevel++;

            } else if (nextEvent.isEndElement()) {
                deepLevel--;
            }
        }
        return new AtomFeed(id, title, description, author, link);
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
                if (!startElement.getName().getPrefix().isBlank()) {
                    continue;
                }

                bldr = switch (startElement.getName().getLocalPart()) {
                    case ENTRY, ITEM -> onItemEntry();
                    case TITLE -> onTitle(bldr, events, i);
                    case CONTENT, DESCRIPTION -> onContentDescription(bldr, events, i);
                    case LINK -> onLink(bldr, feed, events, i);
                    case UPDATED -> onUpdated(bldr, events, i);
                    case PUB_DATE -> onPublicationDate(bldr, events, i);
                    default -> bldr;
                };
            }
        }

        if (bldr == null) {
            return Mono.empty();
        }

        RawNews rawNews = bldr.build();
        if (rawNews.getLink().getScheme() == null
                || !ALLOWED_PROTOCOL.contains(rawNews.getLink().getScheme())) {
            log.warn("Illegal URL detected : {} in feed :{}", rawNews.getLink(), feed.id().substring(0, 10));
            return Mono.empty();
        }

        return Mono.just(rawNews
                .withTitle(HTML_POLICY.sanitize(rawNews.title))
                .withDescription(HTML_POLICY.sanitize(rawNews.description)));
    }

    private RawNews.RawNewsBuilder onItemEntry() {
        log.trace("start parsing entry");
        return RawNews.builder();
    }

    private RawNews.RawNewsBuilder onTitle(RawNews.RawNewsBuilder bldr,
                                           List<XMLEvent> events, int idx) {
        if (bldr == null) {
            return null;
        }
        String title = readElementText(events, idx);
        return bldr.title(title);
    }

    private RawNews.RawNewsBuilder onContentDescription(RawNews.RawNewsBuilder bldr,
                                                        List<XMLEvent> events, int idx) {
        if (bldr == null) {
            return null;
        }
        String content = readElementText(events, idx);
        if (content.isBlank()) {
            return bldr;
        } else {
            return bldr.description(content);
        }
    }

    private RawNews.RawNewsBuilder onLink(RawNews.RawNewsBuilder bldr, ScrapedFeed feed,
                                          List<XMLEvent> events, int idx) {
        if (bldr == null) {
            return null;
        }
        StartElement startElement = events.get(idx).asStartElement();
        String href = Optional.ofNullable(startElement.getAttributeByName(HREF))
                .map(Attribute::getValue)
                .orElseGet(Exceptions.wrap().supplier(() -> readElementText(events, idx)));
        URI link = URI.create(href.trim());
        if (!link.isAbsolute()) {
            link = feed.link().resolve(link);
        }
        return bldr.id(Hasher.identify(link)).link(link);
    }

    private RawNews.RawNewsBuilder onUpdated(RawNews.RawNewsBuilder bldr,
                                             List<XMLEvent> events, int idx) {
        if (bldr == null) {
            return null;
        }
        String updated = readElementText(events, idx);
        Instant updatedAt = DateTimeFormatter.ISO_OFFSET_DATE_TIME.parse(updated, Instant::from);
        return bldr.publication(updatedAt);
    }

    private RawNews.RawNewsBuilder onPublicationDate(RawNews.RawNewsBuilder bldr,
                                                     List<XMLEvent> events, int idx) {
        if (bldr == null) {
            return null;
        }
        String pubDate = readElementText(events, idx);
        Instant datetime = Exceptions.silence().get(() -> DateTimeFormatter.RFC_1123_DATE_TIME.parse(pubDate, Instant::from))
                .orElseGet(() -> DateTimeFormatter.ISO_OFFSET_DATE_TIME.parse(pubDate, Instant::from));
        return bldr.publication(datetime);
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
