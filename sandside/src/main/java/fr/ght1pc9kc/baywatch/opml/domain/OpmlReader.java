package fr.ght1pc9kc.baywatch.opml.domain;

import fr.ght1pc9kc.baywatch.common.domain.Hasher;
import fr.ght1pc9kc.baywatch.techwatch.api.model.WebFeed;
import fr.ght1pc9kc.entity.api.Entity;
import lombok.extern.slf4j.Slf4j;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;

@Slf4j
public class OpmlReader {
    private final Consumer<Entity<WebFeed>> onOutline;
    private final Runnable onComplete;
    private final Consumer<Throwable> onError;

    public OpmlReader(Consumer<Entity<WebFeed>> onOutline, Runnable onComplete, Consumer<Throwable> onError) {
        this.onOutline = onOutline;
        this.onComplete = onComplete;
        this.onError = onError;
    }

    public void read(InputStream is) {
        try {
            XMLEventReader reader = XMLInputFactory.newInstance()
                    .createXMLEventReader(is, StandardCharsets.UTF_8.displayName());
            log.debug("Start reading OPML file ...");
            while (reader.hasNext()) {
                final XMLEvent nextEvent = reader.nextEvent();
                if (nextEvent.isStartElement()) {
                    final StartElement startElement = nextEvent.asStartElement();
                    if (OPMLTags.OUTLINE.equals(startElement.getName().getLocalPart())) {
                        Attribute typeAttribute = startElement.getAttributeByName(QName.valueOf(OPMLTags.ATTRIBUTE_TYPE));
                        if (typeAttribute == null || !OPMLTags.VALUE_TYPE_RSS.equals(typeAttribute.getValue())) {
                            continue;
                        }
                        String text = Optional.ofNullable(startElement.getAttributeByName(QName.valueOf(OPMLTags.ATTRIBUTE_TEXT)))
                                .map(Attribute::getValue)
                                .orElseThrow(() -> new OpmlExecption(OPMLTags.ATTRIBUTE_TEXT + " is a mandatory attribute for outline."));
                        URI uri = Optional.ofNullable(startElement.getAttributeByName(QName.valueOf(OPMLTags.ATTRIBUTE_XML_URL)))
                                .map(Attribute::getValue)
                                .map(URI::create)
                                .orElseThrow(() -> new OpmlExecption(OPMLTags.ATTRIBUTE_XML_URL + " is a mandatory attribute for outline."));
                        Set<String> tags = Optional.ofNullable(startElement.getAttributeByName(QName.valueOf(OPMLTags.ATTRIBUTE_CATEGORY)))
                                .map(Attribute::getValue)
                                .map(t -> Set.of(t.split(",")))
                                .orElseGet(Set::of);

                        Entity<WebFeed> feed = Entity.identify(WebFeed.builder()
                                        .location(uri)
                                        .name(Optional.ofNullable(text).orElseGet(uri::getHost))
                                        .tags(tags)
                                        .build())
                                .withId(Hasher.identify(uri));
                        this.onOutline.accept(feed);
                    }
                }
            }
            log.debug("end of OPML file reach");
            this.onComplete.run();
        } catch (Exception e) {
            log.debug("OPML file read exception -> {}: {}", e.getClass(), e.getLocalizedMessage());
            this.onError.accept(new OpmlExecption("Unable to read OPML Document !", e));
        }
    }
}
