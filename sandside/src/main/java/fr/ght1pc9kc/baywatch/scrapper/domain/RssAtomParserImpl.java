package fr.ght1pc9kc.baywatch.scrapper.domain;

import com.machinezoo.noexception.Exceptions;
import fr.ght1pc9kc.baywatch.common.domain.DateUtils;
import fr.ght1pc9kc.baywatch.scrapper.api.FeedParserPlugin;
import fr.ght1pc9kc.baywatch.scrapper.api.RssAtomParser;
import fr.ght1pc9kc.baywatch.scrapper.infra.config.ScrapperProperties;
import fr.ght1pc9kc.baywatch.techwatch.api.model.Feed;
import fr.ght1pc9kc.baywatch.techwatch.api.model.News;
import fr.ght1pc9kc.baywatch.techwatch.api.model.RawNews;
import fr.ght1pc9kc.baywatch.techwatch.api.model.State;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.owasp.html.PolicyFactory;
import org.owasp.html.Sanitizers;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Component
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

    private final ScrapperProperties properties;
    private final Map<String, FeedParserPlugin> plugins;

    @Setter
    private Clock clock = Clock.systemUTC();

    public RssAtomParserImpl(ScrapperProperties properties, List<FeedParserPlugin> plugins) {
        this.properties = properties;
        this.plugins = plugins.stream()
                .collect(Collectors.toUnmodifiableMap(FeedParserPlugin::pluginForDomain, Function.identity()));
    }

    public Flux<News> parse(Feed feed, InputStream is) {
        final Instant maxAge = DateUtils.toInstant(DateUtils.toLocalDate(clock.instant()).minus(properties.conservation()));

        return Flux.create(sink -> {
            try {
                XMLInputFactory xmlInputFactory = XMLInputFactory.newInstance();
                XMLEventReader reader = xmlInputFactory.createXMLEventReader(is, StandardCharsets.UTF_8.displayName());
                FeedParserPlugin plugin = plugins.get("*");

                RawNews.RawNewsBuilder bldr = null;
                while (reader.hasNext()) {
                    final XMLEvent nextEvent = reader.nextEvent();
                    try {
                        if (nextEvent.isStartElement()) {
                            final StartElement startElement = nextEvent.asStartElement();
                            if (!startElement.getName().getPrefix().isBlank()) {
                                continue;
                            }
                            switch (startElement.getName().getLocalPart()) {
                                case ENTRY, ITEM:
                                    bldr = plugin.handleItemEvent();
                                    break;
                                case TITLE:
                                    if (bldr == null) {
                                        break;
                                    }
                                    String title = reader.getElementText();
                                    bldr = plugin.handleTitleEvent(bldr, title);
                                    break;
                                case CONTENT, DESCRIPTION:
                                    if (bldr == null) {
                                        break;
                                    }
                                    bldr = plugin.handleDescriptionEvent(bldr, reader.getElementText());
                                    break;
                                case LINK:
                                    String href = Optional.ofNullable(startElement.getAttributeByName(HREF))
                                            .map(Attribute::getValue)
                                            .orElseGet(Exceptions.wrap().supplier(reader::getElementText));
                                    URI link = URI.create(href.trim());
                                    if (!link.isAbsolute()) {
                                        link = feed.getUrl().resolve(link);
                                    }
                                    if (bldr == null) {
                                        plugin = plugins.getOrDefault(link.getHost(), plugin);
                                        break;
                                    }
                                    bldr = plugin.handleLinkEvent(bldr, link);
                                    break;
                                case UPDATED:
                                    if (bldr == null) {
                                        break;
                                    }
                                    String updated = reader.getElementText();
                                    Instant updatedAt = DateTimeFormatter.ISO_OFFSET_DATE_TIME.parse(updated, Instant::from);
                                    bldr = plugin.handlePublicationEvent(bldr, updatedAt);
                                    break;
                                case PUB_DATE:
                                    if (bldr == null) {
                                        break;
                                    }
                                    String pubDate = reader.getElementText();
                                    Instant datetime = Exceptions.silence().get(() -> DateTimeFormatter.RFC_1123_DATE_TIME.parse(pubDate, Instant::from))
                                            .orElseGet(() -> DateTimeFormatter.ISO_OFFSET_DATE_TIME.parse(pubDate, Instant::from));
                                    bldr = plugin.handlePublicationEvent(bldr, datetime);
                                    break;
                            }
                        }
                        if (nextEvent.isEndElement() && bldr != null) {
                            final EndElement endElement = nextEvent.asEndElement();
                            String localPart = endElement.getName().getLocalPart();
                            if (ITEM.equals(localPart) || ENTRY.equals(localPart)) {
                                RawNews rawNews = plugin.handleEndEvent(bldr);
                                if (rawNews.getLink().getScheme() == null
                                        || !ALLOWED_PROTOCOL.contains(rawNews.getLink().getScheme())) {
                                    log.warn("Illegal URL detected : {} in feed :{}", rawNews.getLink(), feed.getName());
                                    continue;
                                }
                                RawNews raw = rawNews
                                        .withTitle(HTML_POLICY.sanitize(rawNews.title))
                                        .withDescription(HTML_POLICY.sanitize(rawNews.description));

                                News news = News.builder()
                                        .raw(raw)
                                        .feeds(Set.of(feed.getId()))
                                        .state(State.NONE)
                                        .build();

                                if (news.getPublication().isBefore(maxAge)) {
                                    // Stop scrapping if news was older than purge date
                                    break;
                                }
                                sink.next(news);
                            }
                        }
                    } catch (XMLStreamException e) {
                        log.warn("Error while parsing element of {}", feed.getUrl());
                        log.warn("{}: {}", e.getClass(), e.getLocalizedMessage());
                        log.debug("STACKTRACE", e);
                    }
                }
            } catch (XMLStreamException e) {
                log.warn("Error while parsing {}", feed.getUrl());
                log.warn("{}: {}", e.getClass(), e.getLocalizedMessage());
                log.debug("STACKTRACE", e);
            }
            sink.complete();
        });
    }
}
