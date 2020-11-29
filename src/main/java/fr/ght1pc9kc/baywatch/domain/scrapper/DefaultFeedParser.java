package fr.ght1pc9kc.baywatch.domain.scrapper;

import com.machinezoo.noexception.Exceptions;
import fr.ght1pc9kc.baywatch.api.model.News;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Slf4j
@AllArgsConstructor
public final class DefaultFeedParser {
    private static final DateTimeFormatter FEED_DATE_FORMAT =
            DateTimeFormatter.ofPattern("EEE, d MMM yyyy HH:mm:ss Z", Locale.US);
    private static final String ITEM = "item";
    private static final String TITLE = "title";
    private static final String DESCRIPTION = "description";
    private static final String LINK = "link";
    private static final String PUB_DATE = "pubDate";
    private final InputStream is;

    public Flux<News> itemToFlux() {
        return Flux.create(Exceptions.wrap().consumer(sink -> {
            XMLInputFactory xmlInputFactory = XMLInputFactory.newInstance();
            XMLEventReader reader = xmlInputFactory.createXMLEventReader(is, StandardCharsets.UTF_8.displayName());

            News.NewsBuilder bldr = null;
            while (reader.hasNext()) {
                XMLEvent nextEvent = reader.nextEvent();
                if (nextEvent.isStartElement()) {
                    StartElement startElement = nextEvent.asStartElement();
                    switch (startElement.getName().getLocalPart()) {
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
                            nextEvent = reader.nextEvent();
                            String data = nextEvent.asCharacters().getData();
                            URL url = Exceptions.wrap().get(() -> new URL(data));
                            bldr = bldr.link(url);
                            break;
                        case PUB_DATE:
                            if (bldr == null) {
                                break;
                            }
                            nextEvent = reader.nextEvent();
                            String pubDate = nextEvent.asCharacters().getData();
                            LocalDateTime datetime = LocalDateTime.parse(pubDate, FEED_DATE_FORMAT);
                            bldr = bldr.publication(datetime.toInstant(ZoneOffset.UTC));
                            break;
                    }
                }
                if (nextEvent.isEndElement() && bldr != null) {
                    EndElement endElement = nextEvent.asEndElement();
                    if (endElement.getName().getLocalPart().equals(ITEM)) {
                        sink.next(bldr.build());
                    }
                }
            }
            sink.complete();
        }));
    }
}
