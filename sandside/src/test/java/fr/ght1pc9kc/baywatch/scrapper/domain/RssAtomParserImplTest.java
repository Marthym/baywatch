package fr.ght1pc9kc.baywatch.scrapper.domain;

import fr.ght1pc9kc.baywatch.techwatch.api.model.News;
import fr.ght1pc9kc.baywatch.tests.samples.FeedSamples;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RssAtomParserImplTest {

    private RssAtomParserImpl tested;

    @BeforeEach
    void setUp() {
        tested = new RssAtomParserImpl(List.of());
    }

    @ParameterizedTest(name = "{0}")
    @CsvSource({
            "feeds/journal_du_hacker.xml, item, 1524",
            "feeds/reddit-java.xml, entry, 979",
            "feeds/reddit-prog.xml, entry, 2304",
            "feeds/sebosss.xml, item, 1684",
            "feeds/spring-blog.xml, entry, 14338",
            "feeds/lemonde.xml, item, 688",
    })
    void should_skip_until_entries(String resource, String tag, int expectedCount) {
        Flux<XMLEvent> actual = Flux.fromIterable(toXmlEventList(resource))
                .skipUntil(tested.firstItemEvent());

        StepVerifier.create(actual)
                .expectNextMatches(e -> e.isStartElement() && e.asStartElement().getName().getLocalPart().equals(tag))
                .expectNextCount(expectedCount)
                .verifyComplete();
    }

    @ParameterizedTest(name = "{0}")
    @CsvSource({
            "feeds/journal_du_hacker.xml, item, 24",
            "feeds/reddit-java.xml, entry, 6",
            "feeds/reddit-prog.xml, entry, 24",
            "feeds/sebosss.xml, item, 19",
            "feeds/spring-blog.xml, entry, 19",
            "feeds/lemonde.xml, item, 17",
    })
    void should_bufferize_entries(String resource, String tag, int expectedCount) {
        Flux<List<XMLEvent>> actual = Flux.fromIterable(toXmlEventList(resource))
                .bufferUntil(tested.itemEndEvent())
                .skip(1); // Ignore the first before, it contains all document start event, not skipped

        StepVerifier.create(actual)
                .expectNextMatches(l -> l.get(1).asStartElement().getName().getLocalPart().equals(tag)
                        && l.get(l.size() - 1).asEndElement().getName().getLocalPart().equals(tag))
                .expectNextCount(expectedCount)
                .verifyComplete();
    }

    @Test
    void should_read_rss_item() {
        List<XMLEvent> xmlEvents = toXmlEventList("feeds/rss_item.xml");

        StepVerifier.create(tested.readEntryEvents(xmlEvents, FeedSamples.JEDI))
                .assertNext(actual -> Assertions.assertAll(
                                () -> assertThat(actual).extracting(News::getTitle)
                                        .isEqualTo("DBaaS: Tout comprendre des bases de données managées"),
                                () -> assertThat(actual).extracting(News::getLink)
                                        .isEqualTo(URI.create("https://practicalprogramming.fr/dbaas-la-base-de-donnees-dans-le-cloud/")),
                                () -> assertThat(actual).extracting(News::getPublication)
                                        .isEqualTo(Instant.parse("2020-11-30T15:58:26Z")),
                                () -> assertThat(actual).extracting(News::getDescription)
                                        .isEqualTo("&lt;p&gt;&lt;a href&#61;&#34;https://www.journalduhacker.net/s/vzuiyr/dbaas_tout_comprendre_des_bases_de_donn_es&#34;&gt;Comments&lt;/a&gt;&lt;/p&gt;")
                        )
                ).verifyComplete();
    }

    @Test
    void should_read_atom_entry() {
        List<XMLEvent> xmlEvents = toXmlEventList("feeds/atom_entry.xml");

        StepVerifier.create(tested.readEntryEvents(xmlEvents, FeedSamples.JEDI))
                .assertNext(actual -> Assertions.assertAll(
                                () -> assertThat(actual).extracting(News::getTitle)
                                        .isEqualTo("What&#39;s New in Maven 4 · Maarten on IT"),
                                () -> assertThat(actual).extracting(News::getLink)
                                        .isEqualTo(URI.create("https://www.reddit.com/r/java/comments/k3rv35/whats_new_in_maven_4_maarten_on_it/")),
                                () -> assertThat(actual).extracting(News::getPublication)
                                        .isEqualTo(Instant.parse("2020-11-30T08:20:58Z")),
                                () -> assertThat(actual).extracting(News::getDescription)
                                        .isEqualTo("&amp;#32; submitted by &amp;#32; &lt;a href&#61;&#34;https://www.reddit.com/user/nonusedaccountname&#34;&gt;\n" +
                                                "        /u/nonusedaccountname &lt;/a&gt; &lt;br/&gt; &lt;span&gt;&lt;a href&#61;&#34;https://maarten.mulders.it/2020/11/whats-new-in-maven-4/&#34;&gt;[link]&lt;/a&gt;&lt;/span&gt;\n" +
                                                "        &amp;#32; &lt;span&gt;&lt;a href&#61;&#34;https://www.reddit.com/r/java/comments/k3rv35/whats_new_in_maven_4_maarten_on_it/&#34;&gt;[commentaires]&lt;/a&gt;&lt;/span&gt;")
                        )
                ).verifyComplete();
    }

    @Test
    void should_read_atom_entry_with_html_content() {
        List<XMLEvent> xmlEvents = toXmlEventList("feeds/atom_entry_with_html_content.xml");

        StepVerifier.create(tested.readEntryEvents(xmlEvents, FeedSamples.JEDI))
                .assertNext(actual -> Assertions.assertAll(
                                () -> assertThat(actual).extracting(News::getTitle)
                                        .isEqualTo("Spring Data 2020.0 - New and Noteworthy in Spring Data for Apache Cassandra 3.1"),
                                () -> assertThat(actual).extracting(News::getLink)
                                        .isEqualTo(URI.create("https://spring.io/blog/2020/11/26/spring-data-2020-0-new-and-noteworthy-in-spring-data-for-apache-cassandra-3-1")),
                                () -> assertThat(actual).extracting(News::getPublication)
                                        .isEqualTo(Instant.parse("2020-11-26T14:08:50Z")),
                                () -> assertThat(actual).extracting(News::getDescription)
                                        .asString().startsWith("&lt;p&gt;&lt;a href&#61;&#34;")
                                        .endsWith("&lt;!-- rendered by Sagan Renderer Service --&gt;")
                        )
                ).verifyComplete();
    }

    @Test
    void should_read_rss_item_with_encoded_content() {
        List<XMLEvent> xmlEvents = toXmlEventList("feeds/rss_item_with_encoded_content.xml");

        StepVerifier.create(tested.readEntryEvents(xmlEvents, FeedSamples.JEDI))
                .assertNext(actual -> Assertions.assertAll(
                        () -> assertThat(actual).extracting(News::getTitle)
                                .isEqualTo("Passage à la fibre : c’est mon tour !"),
                        () -> assertThat(actual).extracting(News::getLink)
                                .isEqualTo(URI.create("https://blog.seboss666.info/2020/11/passage-a-la-fibre-cest-mon-tour/")),
                        () -> assertThat(actual).extracting(News::getPublication)
                                .isEqualTo(Instant.parse("2020-11-18T17:00:47Z")),
                        () -> assertThat(actual).extracting(News::getDescription).asString()
                                .startsWith("Et dieu sait que je rageais d&amp;#8217;être le dernier de la famille à ne pas être équipé.")
                                .endsWith("Mon propriétaire à fini par faire les travaux [...]")
                        )
                ).verifyComplete();
    }

    @Test
    void should_read_rss_item_with_cdata() {
        List<XMLEvent> xmlEvents = toXmlEventList("feeds/rss_item_with_cdata.xml");

        StepVerifier.create(tested.readEntryEvents(xmlEvents, FeedSamples.JEDI))
                .assertNext(actual -> Assertions.assertAll(
                                () -> assertThat(actual).extracting(News::getTitle)
                                        .isEqualTo("Des milliers de policiers ont manifesté pour appeler à « protéger ceux qui protègent la République »"),
                                () -> assertThat(actual).extracting(News::getLink)
                                        .isEqualTo(URI.create("https://www.lemonde.fr/societe/article/2021/05/20/des-milliers-de-policiers-ont-manifeste-pour-appeler-a-proteger-ceux-qui-protegent-la-republique_6080801_3224.html")),
                                () -> assertThat(actual).extracting(News::getPublication)
                                        .isEqualTo(Instant.parse("2021-05-20T03:14:23Z")),
                                () -> assertThat(actual).extracting(News::getDescription).asString()
                                        .startsWith("Les principaux syndicats policiers étaient rassemblés, mercredi")
                                        .endsWith("l’institution judiciaire ».")
                        )
                ).verifyComplete();
    }

    @Test
    void should_read_rss_item_with_relative_link() {
        List<XMLEvent> xmlEvents = toXmlEventList("feeds/rss_item_with_relative_link.xml");

        StepVerifier.create(tested.readEntryEvents(xmlEvents, FeedSamples.JEDI))
                .assertNext(actual -> Assertions.assertAll(
                                () -> assertThat(actual).extracting(News::getTitle)
                                        .isEqualTo("DBaaS: Tout comprendre des bases de données managées"),
                                () -> assertThat(actual).extracting(News::getLink)
                                        .isEqualTo(URI.create("https://www.jedi.com/dbaas-la-base-de-donnees-dans-le-cloud/")),
                                () -> assertThat(actual).extracting(News::getPublication)
                                        .isEqualTo(Instant.parse("2020-11-30T15:58:26Z")),
                                () -> assertThat(actual).extracting(News::getDescription)
                                        .isEqualTo("&lt;p&gt;&lt;a href&#61;&#34;https://www.journalduhacker.net/s/vzuiyr/dbaas_tout_comprendre_des_bases_de_donn_es&#34;&gt;Comments&lt;/a&gt;&lt;/p&gt;")
                        )
                ).verifyComplete();
    }

    @Test
    void should_read_rss_item_with_illegal_protocol() {
        List<XMLEvent> xmlEvents = toXmlEventList("feeds/rss_item_with_illegal_protocol.xml");

        StepVerifier.create(tested.readEntryEvents(xmlEvents, FeedSamples.JEDI))
                // Illegal link give empty Mono
                .verifyComplete();
    }

    private static List<XMLEvent> toXmlEventList(String resource) {
        try (InputStream is = RssAtomParserImplTest.class.getResourceAsStream(resource)) {
            XMLInputFactory xmlInputFactory = XMLInputFactory.newInstance();
            XMLEventReader reader = xmlInputFactory.createXMLEventReader(is, StandardCharsets.UTF_8.displayName());
            List<XMLEvent> events = new ArrayList<>();
            while (reader.hasNext()) {
                events.add(reader.nextEvent());
            }
            return List.copyOf(events);
        } catch (IOException | XMLStreamException e) {
            Assertions.fail(e);
            return List.of();
        }
    }
}