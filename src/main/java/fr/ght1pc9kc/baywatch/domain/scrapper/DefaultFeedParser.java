package fr.ght1pc9kc.baywatch.domain.scrapper;

import com.machinezoo.noexception.Exceptions;
import fr.ght1pc9kc.baywatch.api.model.News;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
import java.util.Optional;

@Slf4j
@AllArgsConstructor
public final class DefaultFeedParser {

    private static final String ITEM = "item";
    private static final String ENTRY = "entry";
    private static final String TITLE = "title";
    private static final String DESCRIPTION = "description";
    private static final String CONTENT = "content";
    private static final String LINK = "link";
    private static final String PUB_DATE = "pubDate";
    private static final String UPDATED = "updated";

    public Flux<News> parse(InputStream is) {
        return Flux.create(Exceptions.wrap().consumer(sink -> {
            XMLInputFactory xmlInputFactory = XMLInputFactory.newInstance();
            XMLEventReader reader = xmlInputFactory.createXMLEventReader(is, StandardCharsets.UTF_8.displayName());

            News.NewsBuilder bldr = null;
            while (reader.hasNext()) {
                XMLEvent nextEvent = reader.nextEvent();
                if (nextEvent.isStartElement()) {
                    StartElement startElement = nextEvent.asStartElement();
                    switch (startElement.getName().getLocalPart()) {
                        case ENTRY:
                        case ITEM:
                            bldr = News.builder();
                            break;
                        case TITLE:
                            if (bldr == null) {
                                break;
                            }
                            nextEvent = reader.nextEvent();
                            bldr = bldr.title(nextEvent.asCharacters().getData());
                            break;
                        case CONTENT:
                        case DESCRIPTION:
                            if (bldr == null) {
                                break;
                            }
                            nextEvent = reader.nextEvent();
                            bldr = bldr.description(nextEvent.asCharacters().getData());
                            break;
                        case LINK:
                            if (bldr == null) {
                                break;
                            }
                            String href = Optional.ofNullable(startElement.getAttributeByName(new QName("href")))
                                    .map(Attribute::getValue)
                                    .orElseGet(Exceptions.wrap().supplier(() -> {
                                        XMLEvent next = reader.nextEvent();
                                        return next.asCharacters().getData();
                                    }));
                            bldr = bldr.link(URI.create(href.trim()));
                            break;
                        case UPDATED:
                            if (bldr == null) {
                                break;
                            }
                            nextEvent = reader.nextEvent();
                            String updated = nextEvent.asCharacters().getData();
                            Instant updatedAt = DateTimeFormatter.ISO_OFFSET_DATE_TIME.parse(updated, Instant::from);
                            bldr = bldr.publication(updatedAt);
                            break;
                        case PUB_DATE:
                            if (bldr == null) {
                                break;
                            }
                            nextEvent = reader.nextEvent();
                            String pubDate = nextEvent.asCharacters().getData();
                            Instant datetime = DateTimeFormatter.RFC_1123_DATE_TIME.parse(pubDate, Instant::from);
                            bldr = bldr.publication(datetime);
                            break;
                    }
                }
                if (nextEvent.isEndElement() && bldr != null) {
                    EndElement endElement = nextEvent.asEndElement();
                    String localPart = endElement.getName().getLocalPart();
                    if (ITEM.equals(localPart) || ENTRY.equals(localPart)) {
                        sink.next(bldr.build());
                    }
                }
            }
            sink.complete();
        }));
    }
}
