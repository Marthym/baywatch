package fr.ght1pc9kc.baywatch.domain.opml;

import fr.ght1pc9kc.baywatch.api.techwatch.model.Feed;
import fr.ght1pc9kc.baywatch.api.security.model.User;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.VisibleForTesting;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

@Slf4j
public final class OpmlWriter {
    private static final DateTimeFormatter OPML_DATETIME_FORMATTER = DateTimeFormatter.RFC_1123_DATE_TIME;

    private final XMLStreamWriter xmlWriter;
    private final Clock clock;

    public OpmlWriter(OutputStream out) {
        this(out, Clock.systemUTC());
    }

    @VisibleForTesting
    OpmlWriter(OutputStream out, Clock clock) {
        try {
            this.xmlWriter = XMLOutputFactory.newFactory()
                    .createXMLStreamWriter(out, StandardCharsets.UTF_8.name());
            this.clock = clock;
        } catch (XMLStreamException e) {
            throw new OpmlExecption("Unable to create OPML Document", e);
        }
    }

    public void startOpmlDocument(User owner) {
        try {
            xmlWriter.writeStartDocument();
            xmlWriter.writeStartElement(OPMLTags.OMPL);
            xmlWriter.writeAttribute(OPMLTags.ATTRIBUTE_VERSION, OPMLTags.VALUE_VERSION_2);
            writeHead(xmlWriter, owner);
            xmlWriter.flush();
            xmlWriter.writeStartElement(OPMLTags.BODY);
        } catch (XMLStreamException e) {
            throw new OpmlExecption("Unable to start OPML Document", e);
        }
    }

    public void endOmplDocument() {
        try {
            xmlWriter.writeEndElement(); // body
            xmlWriter.writeEndDocument();
            xmlWriter.flush();
            xmlWriter.close();
        } catch (XMLStreamException e) {
            throw new OpmlExecption("Unable to start OPML Document", e);
        }
        log.debug("OPML Document completed");
    }

    private void writeHead(XMLStreamWriter xmlWriter, User owner) {
        try {
            xmlWriter.writeStartElement(OPMLTags.HEAD);
            xmlWriter.writeStartElement(OPMLTags.TITLE);
            xmlWriter.writeCharacters("Baywatch OPML export");
            xmlWriter.writeEndElement(); // title
            xmlWriter.writeStartElement(OPMLTags.DATE_CREATED);
            xmlWriter.writeCharacters(ZonedDateTime.now(clock).format(OPML_DATETIME_FORMATTER));
            xmlWriter.writeEndElement(); // dateCreated
            xmlWriter.writeStartElement(OPMLTags.OWNER_NAME);
            xmlWriter.writeCharacters(owner.name);
            xmlWriter.writeEndElement(); // ownerName
            xmlWriter.writeStartElement(OPMLTags.OWNER_EMAIL);
            xmlWriter.writeCharacters(owner.mail);
            xmlWriter.writeEndElement(); // ownerEmail
            xmlWriter.writeEndElement(); // head
        } catch (XMLStreamException e) {
            throw new OpmlExecption("Unable to write OPML Document header", e);
        }
    }

    public void writeOutline(Feed feed) {
        try {
            xmlWriter.writeStartElement(OPMLTags.OUTLINE);
            xmlWriter.writeAttribute(OPMLTags.ATTRIBUTE_TEXT, feed.getName());
            xmlWriter.writeAttribute(OPMLTags.ATTRIBUTE_TYPE, OPMLTags.VALUE_TYPE_RSS);
            xmlWriter.writeAttribute(OPMLTags.ATTRIBUTE_XML_URL, feed.getUrl().toString());
            xmlWriter.writeAttribute(OPMLTags.TITLE, feed.getName());
            xmlWriter.writeAttribute(OPMLTags.ATTRIBUTE_CATEGORY, String.join(",", feed.getTags()));
            xmlWriter.writeEndElement(); // outline
            xmlWriter.flush();
        } catch (XMLStreamException e) {
            throw new OpmlExecption("Unable to write OPML Document outline", e);
        }
    }
}
