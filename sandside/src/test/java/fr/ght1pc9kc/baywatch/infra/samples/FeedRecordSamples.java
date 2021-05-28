package fr.ght1pc9kc.baywatch.infra.samples;

import fr.ght1pc9kc.baywatch.domain.utils.Hasher;
import fr.ght1pc9kc.baywatch.dsl.tables.records.FeedsRecord;
import fr.ght1pc9kc.baywatch.dsl.tables.records.FeedsUsersRecord;
import fr.irun.testy.jooq.model.RelationalDataSet;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static fr.ght1pc9kc.baywatch.dsl.tables.Feeds.FEEDS;
import static fr.ght1pc9kc.baywatch.dsl.tables.FeedsUsers.FEEDS_USERS;

public class FeedRecordSamples implements RelationalDataSet<FeedsRecord> {
    public static final FeedRecordSamples SAMPLE = new FeedRecordSamples();

    public static final URI OKENOBI_BASE_URI = URI.create("http://obiwan.kenobi.jedi/");

    public static final FeedsRecord JEDI = FEEDS.newRecord()
            .setFeedId(Hasher.identify(OKENOBI_BASE_URI))
            .setFeedName("Jedi")
            .setFeedUrl(OKENOBI_BASE_URI.toString())
            .setFeedLastWatch(LocalDateTime.parse("2020-12-11T15:12:42"));

    public static final List<FeedsRecord> FEEDS_RECORDS = List.of(
            JEDI,
            JEDI.copy().setFeedId(Hasher.identify(OKENOBI_BASE_URI.resolve("01"))).setFeedUrl(JEDI.getFeedUrl() + "01"),
            JEDI.copy().setFeedId(Hasher.identify(OKENOBI_BASE_URI.resolve("02"))).setFeedUrl(JEDI.getFeedUrl() + "02"),
            JEDI.copy().setFeedId(Hasher.identify(OKENOBI_BASE_URI.resolve("03"))).setFeedUrl(JEDI.getFeedUrl() + "03"),
            JEDI.copy().setFeedId(Hasher.identify(OKENOBI_BASE_URI.resolve("04"))).setFeedUrl(JEDI.getFeedUrl() + "04"),
            JEDI.copy().setFeedId(Hasher.identify(OKENOBI_BASE_URI.resolve("05"))).setFeedUrl(JEDI.getFeedUrl() + "05")
    );

    @Override
    public List<FeedsRecord> records() {
        return FEEDS_RECORDS;
    }

    public static final class FeedUserRecordSamples implements RelationalDataSet<FeedsUsersRecord> {
        public static final FeedUserRecordSamples SAMPLE = new FeedUserRecordSamples();

        public static final List<FeedsUsersRecord> FEEDS_USERS_RECORDS = FEEDS_RECORDS.stream()
                .map(fr -> FEEDS_USERS.newRecord()
                        .setFeusFeedId(fr.getFeedId())
                        .setFeusUserId(UsersRecordSamples.OKENOBI.getUserId()))
                .collect(Collectors.toUnmodifiableList());

        @Override
        public List<FeedsUsersRecord> records() {
            return FEEDS_USERS_RECORDS;
        }
    }
}
