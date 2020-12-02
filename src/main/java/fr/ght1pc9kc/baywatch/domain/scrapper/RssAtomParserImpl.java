package fr.ght1pc9kc.baywatch.domain.scrapper;

import com.machinezoo.noexception.Exceptions;
import fr.ght1pc9kc.baywatch.api.RssAtomParser;
import fr.ght1pc9kc.baywatch.api.model.News;
import fr.ght1pc9kc.baywatch.api.scrapper.FeedParserPlugin;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Component
public final class RssAtomParserImpl implements RssAtomParser {

    private static final String ITEM = "item";
    private static final String ENTRY = "entry";
    private static final String TITLE = "title";
    private static final String DESCRIPTION = "description";
    private static final String CONTENT = "content";
    private static final String LINK = "link";
    private static final String PUB_DATE = "pubDate";
    private static final String UPDATED = "updated";
    private static final QName HREF = new QName("href");

    private final Map<String, FeedParserPlugin> plugins;

    public RssAtomParserImpl(List<FeedParserPlugin> plugins) {
        this.plugins = plugins.stream()
                .collect(Collectors.toUnmodifiableMap(FeedParserPlugin::pluginForDomain, Function.identity()));
    }

    public Flux<News> parse(InputStream is) {
        return Flux.create(Exceptions.wrap().consumer(sink -> {
            XMLInputFactory xmlInputFactory = XMLInputFactory.newInstance();
            XMLEventReader reader = xmlInputFactory.createXMLEventReader(is, StandardCharsets.UTF_8.displayName());
            FeedParserPlugin plugin = plugins.get("*");

            News.NewsBuilder bldr = null;
            while (reader.hasNext()) {
                final XMLEvent nextEvent = reader.nextEvent();
                if (nextEvent.isStartElement()) {
                    final StartElement startElement = nextEvent.asStartElement();
                    XMLEvent textEvent;
                    switch (startElement.getName().getLocalPart()) {
                        case ENTRY:
                        case ITEM:
                            bldr = plugin.handleItemEvent();
                            break;
                        case TITLE:
                            if (bldr == null) {
                                break;
                            }
                            String title = reader.getElementText();
                            bldr = plugin.handleTitleEvent(bldr, title);
                            break;
                        case CONTENT:
                        case DESCRIPTION:
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
                            Instant datetime = DateTimeFormatter.RFC_1123_DATE_TIME.parse(pubDate, Instant::from);
                            bldr = plugin.handlePublicationEvent(bldr, datetime);
                            break;
                    }
                }
                if (nextEvent.isEndElement() && bldr != null) {
                    final EndElement endElement = nextEvent.asEndElement();
                    String localPart = endElement.getName().getLocalPart();
                    if (ITEM.equals(localPart) || ENTRY.equals(localPart)) {
                        sink.next(plugin.handleEndEvent(bldr));
                    }
                }
            }
            sink.complete();
        }));
    }
}
