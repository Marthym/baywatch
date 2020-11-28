package fr.ght1pc9kc.baywatch.scrapper;

import com.machinezoo.noexception.Exceptions;
import fr.ght1pc9kc.baywatch.model.News;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import java.net.URL;

@Slf4j
@AllArgsConstructor
public final class FeedParser {
    private final XMLEventReader reader;

    public Flux<News> itemToFlux() {
        return Flux.create(Exceptions.wrap().consumer(sink -> {
            News.NewsBuilder bldr = null;
            while (reader.hasNext()) {
                XMLEvent nextEvent = reader.nextEvent();
                if (nextEvent.isStartElement()) {
                    StartElement startElement = nextEvent.asStartElement();
                    switch (startElement.getName().getLocalPart()) {
                        case "item":
                            bldr = News.builder();
                            break;
                        case "title":
                            if (bldr == null) {
                                break;
                            }
                            nextEvent = reader.nextEvent();
                            bldr = bldr.title(nextEvent.asCharacters().getData());
                            break;
                        case "description":
                            if (bldr == null) {
                                break;
                            }
                            nextEvent = reader.nextEvent();
                            bldr = bldr.description(nextEvent.asCharacters().getData());
                            break;
                        case "link":
                            if (bldr == null) {
                                break;
                            }
                            nextEvent = reader.nextEvent();
                            String data = nextEvent.asCharacters().getData();
                            URL url = Exceptions.wrap().get(() -> new URL(data));
                            bldr = bldr.link(url);
                            break;
                    }
                }
                if (nextEvent.isEndElement() && bldr != null) {
                    EndElement endElement = nextEvent.asEndElement();
                    if (endElement.getName().getLocalPart().equals("item")) {
                        sink.next(bldr.build());
                    }
                }
            }
            sink.complete();
        }));
    }
}
