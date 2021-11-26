package fr.ght1pc9kc.baywatch.domain.opml;

import com.machinezoo.noexception.Exceptions;
import fr.ght1pc9kc.baywatch.api.FeedService;
import fr.ght1pc9kc.baywatch.api.opml.OpmlService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.nio.charset.StandardCharsets;

@Slf4j
@RequiredArgsConstructor
public class OpmlServiceImpl implements OpmlService {

    private final FeedService feedService;

    @Override
    public Mono<InputStream> export() {
        try {
            PipedInputStream pis = new PipedInputStream();
            PipedOutputStream pos = new PipedOutputStream(pis);
            XMLOutputFactory xmlOutputFactory = XMLOutputFactory.newFactory();
            XMLStreamWriter xmlStreamWriter = xmlOutputFactory.createXMLStreamWriter(pos, StandardCharsets.UTF_8.name());

            return feedService.list()
                    .doFirst(Exceptions.sneak().runnable(() -> {
                        xmlStreamWriter.writeStartDocument();
                        xmlStreamWriter.writeStartElement("opml");
                        xmlStreamWriter.writeAttribute("version", "2.0");
                        xmlStreamWriter.flush();
                    }))
                    .doOnComplete(Exceptions.sneak().runnable(() -> {
                        xmlStreamWriter.writeEndElement();
                        xmlStreamWriter.writeEndDocument();
                        xmlStreamWriter.flush();
                        xmlStreamWriter.close();
                        pos.flush();
                        pos.close();
                        log.debug("Completed OPML");
                    }))
                    .map(feed -> {
                        return feed;
                    }).collectList()
                    .map(_x -> pis);
        } catch (XMLStreamException | IOException e) {
            return Mono.error(e);
        }
    }

    private static void writeHead(XMLStreamWriter xmlWriter) {
        xmlWriter.
    }
}
