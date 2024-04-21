package fr.ght1pc9kc.baywatch.scraper.domain.actions;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import fr.ght1pc9kc.baywatch.scraper.api.model.AtomEntry;
import fr.ght1pc9kc.baywatch.scraper.api.model.AtomFeed;
import fr.ght1pc9kc.baywatch.scraper.api.model.ScrapResult;
import fr.ght1pc9kc.baywatch.scraper.domain.model.ex.FeedScrapingException;
import fr.ght1pc9kc.baywatch.scraper.domain.model.ex.NewsScrapingException;
import fr.ght1pc9kc.baywatch.scraper.domain.model.ex.ScrapingException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import reactor.test.StepVerifier;

import java.net.URI;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class ScrapingLoggerHandlerTest {
    private final ListAppender<ILoggingEvent> mockLogAppender = new ListAppender<>();
    private ScrapingLoggerHandler tested;
    private Logger logger = (Logger) LoggerFactory.getLogger(ScrapingLoggerHandler.class);
    private Level originalLevel;

    @BeforeEach
    void setUp() {
        originalLevel = logger.getLevel();
        logger.setLevel(Level.DEBUG);
        logger.addAppender(mockLogAppender);
        tested = new ScrapingLoggerHandler();
        mockLogAppender.start();
    }

    @AfterEach
    void tearDown() {
        mockLogAppender.stop();
        logger.setLevel(originalLevel);
    }

    @Test
    void should_handler_after() {
        AtomFeed atomFeed = AtomFeed.builder()
                .id("https://spring.io/blog.atom")
                .title("Spring")
                .updated(Instant.EPOCH.plus(Duration.ofDays(1)))
                .link(URI.create("https://jedi.com/feed"))
                .build();
        AtomEntry atomEntry = new AtomEntry(
                "66", "Kylo Ren", null, null, null,
                URI.create("https://jedi.com/news"), Set.of());
        ScrapResult scrapResult = new ScrapResult(3, List.of(
                new FeedScrapingException(atomFeed, new IllegalArgumentException("Feed Error")),
                new NewsScrapingException(atomEntry, new IllegalArgumentException("News error")),
                new ScrapingException("simple", new IllegalArgumentException("simple"))
        ));

        StepVerifier.create(tested.after(scrapResult)).verifyComplete();

        assertThat(mockLogAppender.list).extracting(ILoggingEvent::getFormattedMessage).containsExactly(
                "Scraping finished, 3 news inserted, 3 error(s).",
                "https://jedi.com/feed => class fr.ght1pc9kc.baywatch.scraper.domain.model.ex.FeedScrapingException: Feed Error",
                "STACKTRACE",
                "https://jedi.com/news => class fr.ght1pc9kc.baywatch.scraper.domain.model.ex.NewsScrapingException: News error",
                "STACKTRACE",
                "UNKNOWN => class fr.ght1pc9kc.baywatch.scraper.domain.model.ex.ScrapingException: simple",
                "STACKTRACE"
        );
    }
}