package fr.ght1pc9kc.baywatch.scraper.domain;

import fr.ght1pc9kc.baywatch.scraper.api.model.AtomFeed;
import fr.ght1pc9kc.baywatch.scraper.domain.model.ScrapedFeed;
import fr.ght1pc9kc.baywatch.techwatch.api.model.RawNews;
import fr.ght1pc9kc.baywatch.tests.samples.FeedSamples;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
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
        tested = new RssAtomParserImpl();
    }

    @ParameterizedTest(name = "{0}")
    @CsvSource({
            "feeds/journal_du_hacker.xml, item, 971",
            "feeds/reddit-java.xml, entry, 267",
            "feeds/reddit-prog.xml, entry, 951",
            "feeds/sebosss.xml, item, 1671",
            "feeds/spring-blog.xml, entry, 681",
            "feeds/lemonde.xml, item, 672",
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

    @ParameterizedTest(name = "{0}")
    @CsvSource(delimiter = '|', value = {
            "feeds/journal_du_hacker.xml | | Journal du hacker | | | https://www.journalduhacker.net/",
            "feeds/reddit-java.xml | /r/java/top/.rss | liens vedettes : java |" +
                    "News, Technical discussions, research papers and assorted things of interest related to the Java " +
                    "programming language NO programming help, NO learning Java related questions, NO installing " +
                    "or downloading Java questions, NO JVM languages - Exclusively Java!" +
                    "| | https://www.reddit.com/r/java/top/.rss",
            "feeds/reddit-prog.xml | /r/programming/top/.rss | liens vedettes : programming | Computer Programming | " +
                    "| https://www.reddit.com/r/programming/top/.rss",
            "feeds/sebosss.xml | | Le blog de Seboss666 | Les divagations d'un pseudo-geek curieux " +
                    "| Sebosss" +
                    "| https://blog.seboss666.info/feed/",
            "feeds/spring-blog.xml | https://spring.io/blog.atom | Spring | | | https://spring.io/blog.atom",
            "feeds/lemonde.xml | | Le Monde.fr - Actualités et Infos en France et dans le monde | " +
                    "Le Monde.fr - 1er site d’information. Les articles du journal et toute l’actualité en continu : " +
                    "International, France, Société, Economie, Culture, Environnement, Blogs ... | " +
                    "| https://www.lemonde.fr/rss/une.xml",
            "feeds/feed_uber.xml | | Engineering – Uber Blog | " +
                    "Check out the official blog from Uber to get the latest news, announcements, and things to do in US.\" /> | " +
                    "| https://www.uber.com/",
    })
    void should_read_feed_headers(String resource, String expectedId, String expectedTitle, String expectedDescr,
                                  String expectedAuthor, URI expectedLink) {
        Mono<AtomFeed> actualMono = Flux.fromIterable(toXmlEventList(resource))
                .bufferUntil(tested.firstItemEvent())
                .next()
                .map(tested::readFeedProperties);

        StepVerifier.create(actualMono)
                .assertNext(actual -> Assertions.assertAll(
                        () -> assertThat(actual).extracting(AtomFeed::id).as("ID").isEqualTo(expectedId),
                        () -> assertThat(actual).extracting(AtomFeed::title).as("Title").isEqualTo(expectedTitle),
                        () -> assertThat(actual).extracting(AtomFeed::description).as("Description").isEqualTo(expectedDescr),
                        () -> assertThat(actual).extracting(AtomFeed::author).as("Author").isEqualTo(expectedAuthor),
                        () -> assertThat(actual).extracting(AtomFeed::link).as("Link").isEqualTo(expectedLink)
                )).verifyComplete();
    }

    @ParameterizedTest
    @CsvSource({
            "feeds/rss_item.xml, " +
                    "DBaaS: Tout comprendre des bases de données managées, " +
                    "https://practicalprogramming.fr/dbaas-la-base-de-donnees-dans-le-cloud/, " +
                    "2020-11-30T15:58:26Z, " +
                    "Comments",
            "feeds/rss_item_pubdate_format.xml, " +
                    "MySQL to MyRocks Migration in Uber’s Distributed Datastores, " +
                    "https://www.uber.com/blog/mysql-to-myrocks-migration-in-uber-distributed-datastores/, " +
                    "2022-09-01T16:30:00Z, " +
                    "Uber’s Storage Platform team talks about the massive strategic undertaking to migrate their " +
                    "Distributed Databases from MySQL to MyRocks resulting in significant Storage usage reduction. The blog details the migration process and challenges faced.",
    })
    void should_read_rss_item(String inputFileName, String expectedTitle, String expectedUrl, String expectedPubDate, String expectedDescription) {
        List<XMLEvent> xmlEvents = toXmlEventList(inputFileName);

        ScrapedFeed sampleFeed = new ScrapedFeed(FeedSamples.JEDI.getRaw().id, FeedSamples.JEDI.getRaw().url);
        StepVerifier.create(tested.readEntryEvents(xmlEvents, sampleFeed))
                .assertNext(actual -> Assertions.assertAll(
                        () -> assertThat(actual).extracting(RawNews::getTitle).isEqualTo(expectedTitle),
                        () -> assertThat(actual).extracting(RawNews::getLink).isEqualTo(URI.create(expectedUrl)),
                        () -> assertThat(actual).extracting(RawNews::getPublication).isEqualTo(Instant.parse(expectedPubDate)),
                        () -> assertThat(actual).extracting(RawNews::getDescription).isEqualTo(expectedDescription)
                )).verifyComplete();
    }

    @Test
    void should_read_atom_entry() {
        List<XMLEvent> xmlEvents = toXmlEventList("feeds/atom_entry.xml");

        ScrapedFeed sampleFeed = new ScrapedFeed(FeedSamples.JEDI.getRaw().id, FeedSamples.JEDI.getRaw().url);
        StepVerifier.create(tested.readEntryEvents(xmlEvents, sampleFeed))
                .assertNext(actual -> Assertions.assertAll(
                        () -> assertThat(actual).extracting(RawNews::getTitle)
                                .isEqualTo("What's New in Maven 4 · Maarten on IT"),
                        () -> assertThat(actual).extracting(RawNews::getLink)
                                .isEqualTo(URI.create("https://www.reddit.com/r/java/comments/k3rv35/whats_new_in_maven_4_maarten_on_it/")),
                        () -> assertThat(actual).extracting(RawNews::getPublication)
                                .isEqualTo(Instant.parse("2020-11-30T08:20:58Z")),
                        () -> assertThat(actual).extracting(RawNews::getDescription)
                                .isEqualTo("submitted by /u/nonusedaccountname <br /> [link] [commentaires]")
                )).verifyComplete();
    }

    @Test
    void should_read_atom_entry_with_html_content() {
        List<XMLEvent> xmlEvents = toXmlEventList("feeds/atom_entry_with_html_content.xml");

        ScrapedFeed sampleFeed = new ScrapedFeed(FeedSamples.JEDI.getRaw().id, FeedSamples.JEDI.getRaw().url);
        StepVerifier.create(tested.readEntryEvents(xmlEvents, sampleFeed))
                .assertNext(actual -> Assertions.assertAll(
                                () -> assertThat(actual).extracting(RawNews::getTitle)
                                        .isEqualTo("Spring Data 2020.0 - New and Noteworthy in Spring Data for Apache Cassandra 3.1"),
                                () -> assertThat(actual).extracting(RawNews::getLink)
                                        .isEqualTo(URI.create("https://spring.io/blog/2020/11/26/spring-data-2020-0-new-and-noteworthy-in-spring-data-for-apache-cassandra-3-1")),
                                () -> assertThat(actual).extracting(RawNews::getPublication)
                                        .isEqualTo(Instant.parse("2020-11-26T14:08:50Z")),
                                () -> assertThat(actual).extracting(RawNews::getDescription)
                                        .asString().startsWith("Spring Data <code>2020.0.0</code> ")
                                        .endsWith("of these accept <code>RowMapper</code>.")
                        )
                ).verifyComplete();
    }

    @Test
    void should_read_rss_item_with_encoded_content() {
        List<XMLEvent> xmlEvents = toXmlEventList("feeds/rss_item_with_encoded_content.xml");

        ScrapedFeed sampleFeed = new ScrapedFeed(FeedSamples.JEDI.getRaw().id, FeedSamples.JEDI.getRaw().url);
        StepVerifier.create(tested.readEntryEvents(xmlEvents, sampleFeed))
                .assertNext(actual -> Assertions.assertAll(
                                () -> assertThat(actual).extracting(RawNews::getTitle)
                                        .isEqualTo("Passage à la fibre : c’est mon tour !"),
                                () -> assertThat(actual).extracting(RawNews::getLink)
                                        .isEqualTo(URI.create("https://blog.seboss666.info/2020/11/passage-a-la-fibre-cest-mon-tour/")),
                                () -> assertThat(actual).extracting(RawNews::getPublication)
                                        .isEqualTo(Instant.parse("2020-11-18T17:00:47Z")),
                                () -> assertThat(actual).extracting(RawNews::getDescription).asString()
                                        .startsWith("Et dieu sait que je rageais d’être le dernier de la famille à ne pas être équipé.")
                                        .endsWith("Mon propriétaire à fini par faire les travaux [...]")
                        )
                ).verifyComplete();
    }

    @Test
    void should_read_rss_item_with_cdata() {
        List<XMLEvent> xmlEvents = toXmlEventList("feeds/rss_item_with_cdata.xml");

        ScrapedFeed sampleFeed = new ScrapedFeed(FeedSamples.JEDI.getRaw().id, FeedSamples.JEDI.getRaw().url);
        StepVerifier.create(tested.readEntryEvents(xmlEvents, sampleFeed))
                .assertNext(actual -> Assertions.assertAll(
                                () -> assertThat(actual).extracting(RawNews::getTitle)
                                        .isEqualTo("Des milliers de policiers ont manifesté pour appeler à « protéger ceux qui protègent la République »"),
                                () -> assertThat(actual).extracting(RawNews::getLink)
                                        .isEqualTo(URI.create("https://www.lemonde.fr/societe/article/2021/05/20/des-milliers-de-policiers-ont-manifeste-pour-appeler-a-proteger-ceux-qui-protegent-la-republique_6080801_3224.html")),
                                () -> assertThat(actual).extracting(RawNews::getPublication)
                                        .isEqualTo(Instant.parse("2021-05-20T03:14:23Z")),
                                () -> assertThat(actual).extracting(RawNews::getDescription).asString()
                                        .startsWith("Les principaux syndicats policiers étaient rassemblés, mercredi")
                                        .endsWith("l’institution judiciaire ».")
                        )
                ).verifyComplete();
    }

    @Test
    void should_read_rss_item_with_relative_link() {
        List<XMLEvent> xmlEvents = toXmlEventList("feeds/rss_item_with_relative_link.xml");

        ScrapedFeed sampleFeed = new ScrapedFeed(FeedSamples.JEDI.getRaw().id, FeedSamples.JEDI.getRaw().url);
        StepVerifier.create(tested.readEntryEvents(xmlEvents, sampleFeed))
                .assertNext(actual -> Assertions.assertAll(
                        () -> assertThat(actual).extracting(RawNews::getTitle)
                                .isEqualTo("DBaaS: Tout comprendre des bases de données managées"),
                        () -> assertThat(actual).extracting(RawNews::getLink)
                                .isEqualTo(URI.create("https://www.jedi.com/dbaas-la-base-de-donnees-dans-le-cloud/")),
                        () -> assertThat(actual).extracting(RawNews::getPublication)
                                .isEqualTo(Instant.parse("2020-11-30T15:58:26Z")),
                        () -> assertThat(actual).extracting(RawNews::getDescription).isEqualTo("Comments")
                )).verifyComplete();
    }

    @Test
    void should_read_rss_item_with_illegal_protocol() {
        List<XMLEvent> xmlEvents = toXmlEventList("feeds/rss_item_with_illegal_protocol.xml");
        ScrapedFeed sampleFeed = new ScrapedFeed(FeedSamples.JEDI.getRaw().id, FeedSamples.JEDI.getRaw().url);
        StepVerifier.create(tested.readEntryEvents(xmlEvents, sampleFeed))
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