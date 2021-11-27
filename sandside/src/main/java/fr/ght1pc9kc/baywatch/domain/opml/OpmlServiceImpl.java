package fr.ght1pc9kc.baywatch.domain.opml;

import com.machinezoo.noexception.Exceptions;
import fr.ght1pc9kc.baywatch.api.FeedService;
import fr.ght1pc9kc.baywatch.api.model.Feed;
import fr.ght1pc9kc.baywatch.api.opml.OpmlService;
import fr.ght1pc9kc.baywatch.api.security.model.User;
import fr.ght1pc9kc.baywatch.domain.exceptions.UnauthenticatedUser;
import fr.ght1pc9kc.baywatch.domain.ports.AuthenticationFacade;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

@Slf4j
@RequiredArgsConstructor
public class OpmlServiceImpl implements OpmlService {
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.RFC_1123_DATE_TIME;
    private final FeedService feedService;
    private final AuthenticationFacade authFacade;
    private final Clock clock;

    @Override
    public Mono<InputStream> export() {
        try {
            PipedInputStream pis = new PipedInputStream();
            PipedOutputStream pos = new PipedOutputStream(pis);
            XMLOutputFactory xmlOutputFactory = XMLOutputFactory.newFactory();
            XMLStreamWriter xmlWriter = xmlOutputFactory.createXMLStreamWriter(pos, StandardCharsets.UTF_8.name());

            return authFacade.getConnectedUser()
                    .switchIfEmpty(Mono.error(new UnauthenticatedUser("Authentication not found !")))
                    .doOnNext(owner -> writeOpml(xmlWriter, owner)
                            .doOnTerminate(Exceptions.sneak().runnable(() -> {
                                pos.flush();
                                pos.close();
                            }))
                            .subscribe()
                    ).then(Mono.just(pis));
        } catch (XMLStreamException | IOException e) {
            return Mono.error(e);
        }
    }

    private Mono<Void> writeOpml(XMLStreamWriter xmlWriter, User owner) {

        return feedService.list()
                .doFirst(Exceptions.sneak().runnable(() -> {
                    xmlWriter.writeStartDocument();
                    xmlWriter.writeStartElement("opml");
                    xmlWriter.writeAttribute("version", "2.0");
                    writeHead(xmlWriter, owner);
                    xmlWriter.flush();
                    xmlWriter.writeStartElement("body");
                }))
                .doOnEach(signal -> {
                    Feed feed = signal.get();
                    if (feed != null) {
                        writeOutline(xmlWriter, feed);
                    }
                })
                .doOnComplete(Exceptions.sneak().runnable(() -> {
                            xmlWriter.writeEndElement(); // body
                            xmlWriter.writeEndDocument();
                            xmlWriter.flush();
                            xmlWriter.close();
                            log.debug("Completed OPML");
                        })
                ).onErrorResume(Exceptions.sneak().function(e -> {
                    xmlWriter.writeEndDocument();
                    xmlWriter.flush();
                    xmlWriter.close();
                    log.error("{}: {}", e.getClass(), e.getLocalizedMessage());
                    log.debug("STACKTRACE", e);
                    return Flux.empty();
                }))
                .contextWrite(context -> authFacade.withAuthentication(owner))
                .then();
    }

    private void writeHead(XMLStreamWriter xmlWriter, User owner) {
        try {
            xmlWriter.writeStartElement("head");
            xmlWriter.writeStartElement("title");
            xmlWriter.writeCharacters("Baywatch OPML export");
            xmlWriter.writeEndElement(); // title
            xmlWriter.writeStartElement("dateCreated");
            xmlWriter.writeCharacters(ZonedDateTime.now(clock).format(DATE_TIME_FORMATTER));
            xmlWriter.writeEndElement(); // dateCreated
            xmlWriter.writeStartElement("ownerName");
            xmlWriter.writeCharacters(owner.name);
            xmlWriter.writeEndElement(); // ownerName
            xmlWriter.writeStartElement("ownerEmail");
            xmlWriter.writeCharacters(owner.mail);
            xmlWriter.writeEndElement(); // ownerEmail
            xmlWriter.writeEndElement(); // head
        } catch (XMLStreamException e) {
            log.error("{}: {}", e.getClass(), e.getLocalizedMessage());
            log.debug("STACKTRACE", e);
        }
    }

    private void writeOutline(XMLStreamWriter xmlWriter, Feed feed) {
        try {
            xmlWriter.writeStartElement("outline");
            xmlWriter.writeAttribute("text", feed.getId());
            xmlWriter.writeAttribute("type", "rss");
            xmlWriter.writeAttribute("xmlUrl", feed.getUrl().toString());
            xmlWriter.writeAttribute("title", feed.getName());
            xmlWriter.writeAttribute("category", String.join(",", feed.getTags()));
            xmlWriter.writeEndElement(); // outline
        } catch (XMLStreamException e) {
            log.error("{}: {}", e.getClass(), e.getLocalizedMessage());
            log.debug("STACKTRACE", e);
        }
    }
}
