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
                        && l.getLast().asEndElement().getName().getLocalPart().equals(tag))
                .expectNextCount(expectedCount)
                .verifyComplete();
    }

    @ParameterizedTest(name = "{0}")
    @CsvSource(delimiter = '|', value = {
            "feeds/journal_du_hacker.xml | bb03e8fce1f61cc798715c01aa3a5483804d309d34c2d1c71307df1ca955a360 | " +
                    "Journal du hacker | | | https://www.journalduhacker.net/",
            "feeds/reddit-java.xml | c4b6c33021f80a6c3bb581ed2649188fe9e2cd35012cbe515c9edd78cbc1ab5f | " +
                    "liens vedettes : java |" +
                    "News, Technical discussions, research papers and assorted things of interest related to the Java " +
                    "programming language NO programming help, NO learning Java related questions, NO installing " +
                    "or downloading Java questions, NO JVM languages - Exclusively Java!" +
                    "| | https://www.reddit.com/r/java/top/.rss",
            "feeds/reddit-prog.xml | 16195e35f712d366b1309be2715a3a2feadaae0968348f0a52b1afefef6930c7 | " +
                    "liens vedettes : programming | Computer Programming | " +
                    "| https://www.reddit.com/r/programming/top/.rss",
            "feeds/sebosss.xml | 4e1014585a204514ed15ba563e3ea568ab6037df94d4ab26e02a1304eb65323f | " +
                    "Le blog de Seboss666 | Les divagations d'un pseudo-geek curieux " +
                    "| Sebosss <blog@seboss66.info>" +
                    "| https://blog.seboss666.info/feed/",
            "feeds/spring-blog.xml | 1f552d1f31fcf8ff59bc05a035e4f22733cbed8b4aba61d008d93d37e8326cd4 | " +
                    "Spring | | | https://spring.io/blog.atom",
            "feeds/lemonde.xml | 3055bdc996f9992943ee12460f4985f3b4f95a5edd7cb68d8c70b76e5ec78a47 | " +
                    "Le Monde.fr - Actualités et Infos en France et dans le monde | " +
                    "Le Monde.fr - 1er site d’information. Les articles du journal et toute l’actualité en continu : " +
                    "International, France, Société, Economie, Culture, Environnement, Blogs ... | " +
                    "| https://www.lemonde.fr/rss/une.xml",
            "feeds/feed_uber.xml | faa64d2482693a08f7fafa8b3d15873450a53c4f8899edce5b6841fa1cb4d88f | " +
                    "Engineering &#8211; Uber Blog | " +
                    "Check out the official blog from Uber to get the latest news, announcements, and things to do in US.\" /><meta name=\"robots\" content=\"index, follow, max-snippet:-1, max-image-preview:large, max-video-preview:-1\" /><link rel=\"canonical\" href=\"https://www.uber.com/blog/\" /><meta property=\"og:locale\" content=\"en_US\" /><meta property=\"og:type\" content=\"article\" /><meta property=\"og:title\" content=\"US Archives\" /><meta property=\"og:description\" content=\"Check out the official blog from Uber to get the latest news, announcements, and things to do in your community.\" /><meta property=\"og:url\" content=\"https://www.uber.com/blog/\" /><meta property=\"og:site_name\" content=\"Uber Blog\" /><meta property=\"og:image\" content=\"https://blog.uber-cdn.com/cdn-cgi/image/width=500,height=300,quality=80,onerror=redirect,format=auto/wp-content/uploads/2018/09/uber_blog_seo.png\" /><meta property=\"og:image:width\" content=\"500\" /><meta property=\"og:image:height\" content=\"300\" /><meta property=\"og:image:type\" content=\"image/png\" /><meta name=\"twitter:card\" content=\"summary_large_image\" /><meta property=\"fb:pages\" content=\"120945717945722" +
                    "| | https://www.uber.com/",
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
                    "&lt;p>&lt;a href=\"https://www.journalduhacker.net/s/vzuiyr/dbaas_tout_comprendre_des_bases_de_donn_es\">Comments&lt;/a>&lt;/p>",
            "feeds/rss_item_pubdate_format.xml, " +
                    "MySQL to MyRocks Migration in Uber&#8217;s Distributed Datastores, " +
                    "https://www.uber.com/blog/mysql-to-myrocks-migration-in-uber-distributed-datastores/, " +
                    "2022-09-01T16:30:00Z, " +
                    "<p>Uber&#8217;s Storage Platform team talks about the massive strategic undertaking to migrate " +
                    "their Distributed Databases from MySQL to MyRocks resulting in significant Storage usage reduction. " +
                    "The blog details the migration process and challenges faced.</p>",
            "feeds/debian_io.xml, " +
                    "Matrix-Synapse : migrer de SQLite à PostgreSQL, " +
                    "https://www.deblan.io/post/655/matrix-synapse-migrer-de-sqlite-a-postgresql, " +
                    "2023-09-10T16:30:00Z, " +
                    "<p>Matrix-Synapse est un service de messagerie décentralisé et interopérable avec d'autres " +
                    "messageries. Je l'utilise à titre personnel et dans le cadre d'une association. Il livre un service " +
                    "que je considère sensible, c'est pourquoi les différentes instances sont hébergées sur des infras " +
                    "que je gère.</p>"
    })
    void should_read_rss_item(String inputFileName, String expectedTitle, String expectedUrl, String expectedPubDate, String expectedDescription) {
        List<XMLEvent> xmlEvents = toXmlEventList(inputFileName);

        ScrapedFeed sampleFeed = new ScrapedFeed(FeedSamples.JEDI.id(), FeedSamples.JEDI.self().location(),
                Instant.parse("2024-02-25T17:15:42Z"), null);
        StepVerifier.create(tested.readEntryEvents(xmlEvents, sampleFeed))
                .assertNext(actual -> Assertions.assertAll(
                        () -> assertThat(actual).extracting(RawNews::title).isEqualTo(expectedTitle),
                        () -> assertThat(actual).extracting(RawNews::link).isEqualTo(URI.create(expectedUrl)),
                        () -> assertThat(actual).extracting(RawNews::publication).isEqualTo(Instant.parse(expectedPubDate)),
                        () -> assertThat(actual).extracting(RawNews::description).asString().startsWith(expectedDescription)
                )).verifyComplete();
    }

    @Test
    void should_read_atom_entry() {
        List<XMLEvent> xmlEvents = toXmlEventList("feeds/atom_entry.xml");

        ScrapedFeed sampleFeed = new ScrapedFeed(FeedSamples.JEDI.id(), FeedSamples.JEDI.self().location(),
                Instant.parse("2024-02-25T17:15:42Z"), null);
        StepVerifier.create(tested.readEntryEvents(xmlEvents, sampleFeed))
                .assertNext(actual -> Assertions.assertAll(
                        () -> assertThat(actual).extracting(RawNews::title)
                                .isEqualTo("What's New in Maven 4 · Maarten on IT"),
                        () -> assertThat(actual).extracting(RawNews::link)
                                .isEqualTo(URI.create("https://www.reddit.com/r/java/comments/k3rv35/whats_new_in_maven_4_maarten_on_it/")),
                        () -> assertThat(actual).extracting(RawNews::publication)
                                .isEqualTo(Instant.parse("2020-11-30T08:20:58Z")),
                        () -> assertThat(actual).extracting(RawNews::description)
                                .isEqualTo("&amp;#32; submitted by &amp;#32; &lt;a " +
                                        "href=\"https://www.reddit.com/user/nonusedaccountname\"> /u/nonusedaccountname " +
                                        "&lt;/a> &lt;br/> &lt;span>&lt;a href=\"https://maarten.mulders.it/2020/11/whats-new-in-maven-4/\">" +
                                        "[link]&lt;/a>&lt;/span> &amp;#32; &lt;span>&lt;a " +
                                        "href=\"https://www.reddit.com/r/java/comments/k3rv35/whats_new_in_maven_4_maarten_on_it/\">" +
                                        "[commentaires]&lt;/a>&lt;/span>")
                )).verifyComplete();
    }

    @Test
    void should_read_atom_entry_with_html_content() {
        List<XMLEvent> xmlEvents = toXmlEventList("feeds/atom_entry_with_html_content.xml");

        ScrapedFeed sampleFeed = new ScrapedFeed(FeedSamples.JEDI.id(), FeedSamples.JEDI.self().location(),
                Instant.parse("2024-02-25T17:15:42Z"), null);
        StepVerifier.create(tested.readEntryEvents(xmlEvents, sampleFeed))
                .assertNext(actual -> Assertions.assertAll(
                                () -> assertThat(actual).extracting(RawNews::title)
                                        .isEqualTo("Spring Data 2020.0 - New and Noteworthy in Spring Data for Apache Cassandra 3.1"),
                                () -> assertThat(actual).extracting(RawNews::link)
                                        .isEqualTo(URI.create("https://spring.io/blog/2020/11/26/spring-data-2020-0-new-and-noteworthy-in-spring-data-for-apache-cassandra-3-1")),
                                () -> assertThat(actual).extracting(RawNews::publication)
                                        .isEqualTo(Instant.parse("2020-11-26T14:08:50Z")),
                                () -> assertThat(actual).extracting(RawNews::description)
                                        .asString().startsWith("&lt;p>&lt;a href=\"https://")
                                        .endsWith("&lt;!-- rendered by Sagan Renderer Service -->")
                        )
                ).verifyComplete();
    }

    @Test
    void should_read_rss_item_with_encoded_content() {
        List<XMLEvent> xmlEvents = toXmlEventList("feeds/rss_item_with_encoded_content.xml");

        ScrapedFeed sampleFeed = new ScrapedFeed(FeedSamples.JEDI.id(), FeedSamples.JEDI.self().location(),
                Instant.parse("2024-02-25T17:15:42Z"), null);
        StepVerifier.create(tested.readEntryEvents(xmlEvents, sampleFeed))
                .assertNext(actual -> Assertions.assertAll(
                                () -> assertThat(actual).extracting(RawNews::title)
                                        .isEqualTo("Passage à la fibre : c’est mon tour !"),
                                () -> assertThat(actual).extracting(RawNews::link)
                                        .isEqualTo(URI.create("https://blog.seboss666.info/2020/11/passage-a-la-fibre-cest-mon-tour/")),
                                () -> assertThat(actual).extracting(RawNews::publication)
                                        .isEqualTo(Instant.parse("2020-11-18T17:00:47Z")),
                                () -> assertThat(actual).extracting(RawNews::description).asString()
                                        .startsWith("Et dieu sait que je rageais d&#8217;être")
                                        .endsWith("Mon propriétaire à fini par faire les travaux [...]")
                        )
                ).verifyComplete();
    }

    @Test
    void should_read_rss_item_with_cdata() {
        List<XMLEvent> xmlEvents = toXmlEventList("feeds/rss_item_with_cdata.xml");

        ScrapedFeed sampleFeed = new ScrapedFeed(FeedSamples.JEDI.id(), FeedSamples.JEDI.self().location(),
                Instant.parse("2024-02-25T17:15:42Z"), null);
        StepVerifier.create(tested.readEntryEvents(xmlEvents, sampleFeed))
                .assertNext(actual -> Assertions.assertAll(
                                () -> assertThat(actual).extracting(RawNews::title)
                                        .isEqualTo("Des milliers de policiers ont manifesté pour appeler à « protéger ceux qui protègent la République »"),
                                () -> assertThat(actual).extracting(RawNews::link)
                                        .isEqualTo(URI.create("https://www.lemonde.fr/societe/article/2021/05/20/des-milliers-de-policiers-ont-manifeste-pour-appeler-a-proteger-ceux-qui-protegent-la-republique_6080801_3224.html")),
                                () -> assertThat(actual).extracting(RawNews::publication)
                                        .isEqualTo(Instant.parse("2021-05-20T03:14:23Z")),
                                () -> assertThat(actual).extracting(RawNews::description).asString()
                                        .startsWith("Les principaux syndicats policiers étaient rassemblés, mercredi")
                                        .endsWith("l’institution judiciaire ».")
                        )
                ).verifyComplete();
    }

    @Test
    void should_read_rss_item_with_relative_link() {
        List<XMLEvent> xmlEvents = toXmlEventList("feeds/rss_item_with_relative_link.xml");

        ScrapedFeed sampleFeed = new ScrapedFeed(FeedSamples.JEDI.id(), FeedSamples.JEDI.self().location(),
                Instant.parse("2024-02-25T17:15:42Z"), null);
        StepVerifier.create(tested.readEntryEvents(xmlEvents, sampleFeed))
                .assertNext(actual -> Assertions.assertAll(
                        () -> assertThat(actual).extracting(RawNews::title)
                                .isEqualTo("DBaaS: Tout comprendre des bases de données managées"),
                        () -> assertThat(actual).extracting(RawNews::link)
                                .isEqualTo(URI.create("https://www.jedi.com/dbaas-la-base-de-donnees-dans-le-cloud/")),
                        () -> assertThat(actual).extracting(RawNews::publication)
                                .isEqualTo(Instant.parse("2020-11-30T15:58:26Z")),
                        () -> assertThat(actual).extracting(RawNews::description).isEqualTo(
                                "&lt;p>&lt;a href=\"https://www.journalduhacker.net/s/vzuiyr/dbaas_tout_comprendre_des_bases_de_donn_es\">Comments&lt;/a>&lt;/p>")
                )).verifyComplete();
    }

    @Test
    void should_read_rss_item_with_illegal_protocol() {
        List<XMLEvent> xmlEvents = toXmlEventList("feeds/rss_item_with_illegal_protocol.xml");
        ScrapedFeed sampleFeed = new ScrapedFeed(FeedSamples.JEDI.id(), FeedSamples.JEDI.self().location(),
                Instant.parse("2024-02-25T17:15:42Z"), null);
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