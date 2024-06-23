package fr.ght1pc9kc.baywatch.opml.domain;

import fr.ght1pc9kc.baywatch.tests.samples.FeedSamples;
import fr.ght1pc9kc.baywatch.tests.samples.UserSamples;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

class OpmlWriterTest {
    @Test
    void should_write_opml_document() {
        ByteArrayOutputStream actual = new ByteArrayOutputStream();

        OpmlWriter tested = new OpmlWriter(actual, Clock.fixed(Instant.parse("2024-06-30T17:03:42Z"), ZoneOffset.UTC));

        tested.startOpmlDocument(UserSamples.OBIWAN.self());

        tested.writeOutline(FeedSamples.JEDI.self());
        tested.writeOutline(FeedSamples.SITH.self());

        tested.endOmplDocument();

        Assertions.assertThat(actual.toString()).isEqualToIgnoringWhitespace("""
                <?xml version='1.0' encoding='UTF-8'?>
                <opml version="2.0">
                    <head>
                        <title>Baywatch OPML export</title>
                        <dateCreated>Sun, 30 Jun 2024 17:03:42 GMT</dateCreated>
                        <ownerName>Obiwan Kenobi</ownerName>
                        <ownerEmail>obiwan.kenobi@jedi.com</ownerEmail>
                    </head>
                    <body>
                        <outline text="Jedi Feed"
                            type="rss" xmlUrl="https://www.jedi.com/"
                            title="Jedi Feed" category=""/>
                        <outline text="Sith Feed"
                            type="rss" xmlUrl="https://www.sith.com/"
                            title="Sith Feed" category=""/>
                    </body>
                </opml>
                """);
    }
}