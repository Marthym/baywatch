package fr.ght1pc9kc.baywatch.tests.samples.infra;

import com.google.common.base.Joiner;
import fr.ght1pc9kc.baywatch.common.domain.Hasher;
import fr.ght1pc9kc.baywatch.dsl.tables.records.FeedsRecord;
import fr.ght1pc9kc.baywatch.dsl.tables.records.FeedsUsersRecord;
import fr.irun.testy.jooq.model.RelationalDataSet;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static fr.ght1pc9kc.baywatch.dsl.tables.Feeds.FEEDS;
import static fr.ght1pc9kc.baywatch.dsl.tables.FeedsUsers.FEEDS_USERS;

public class FeedRecordSamples implements RelationalDataSet<FeedsRecord> {
    public static final FeedRecordSamples SAMPLE = new FeedRecordSamples();

    public static final URI JEDI_BASE_URI = URI.create("http://www.jedi.light/");

    public static final FeedsRecord JEDI = FEEDS.newRecord()
            .setFeedId(Hasher.identify(JEDI_BASE_URI))
            .setFeedName("Jedi")
            .setFeedUrl(JEDI_BASE_URI.toString())
            .setFeedLastWatch(LocalDateTime.parse("2020-12-11T15:12:42"));

    public static final List<FeedsRecord> FEEDS_RECORDS = List.of(
            JEDI,
            JEDI.copy().setFeedId(Hasher.identify(JEDI_BASE_URI.resolve("01"))).setFeedUrl(JEDI.getFeedUrl() + "01"),
            JEDI.copy().setFeedId(Hasher.identify(JEDI_BASE_URI.resolve("02"))).setFeedUrl(JEDI.getFeedUrl() + "02"),
            JEDI.copy().setFeedId(Hasher.identify(JEDI_BASE_URI.resolve("03"))).setFeedUrl(JEDI.getFeedUrl() + "03"),
            JEDI.copy().setFeedId(Hasher.identify(JEDI_BASE_URI.resolve("04"))).setFeedUrl(JEDI.getFeedUrl() + "04"),
            JEDI.copy().setFeedId(Hasher.identify(JEDI_BASE_URI.resolve("05"))).setFeedUrl(JEDI.getFeedUrl() + "05")
    );

    @Override
    public List<FeedsRecord> records() {
        return FEEDS_RECORDS;
    }

    public static final class FeedUserRecordSamples implements RelationalDataSet<FeedsUsersRecord> {
        public static final FeedUserRecordSamples SAMPLE = new FeedUserRecordSamples();

        public static final List<FeedsUsersRecord> FEEDS_USERS_RECORDS;
        private static final List<String> tags = List.of("jedi", "sith", "light", "dark", "republic", "empire");

        static {
            List<FeedsUsersRecord> feedsUsersRecords = new ArrayList<>();
            int idx = 0;
            for (FeedsRecord fr : FEEDS_RECORDS) {
                if (idx == 0) {
                    ++idx;
                    continue;
                }
                feedsUsersRecords.add(FEEDS_USERS.newRecord()
                        .setFeusFeedId(fr.getFeedId())
                        .setFeusUserId(UsersRecordSamples.OKENOBI.getUserId())
                        .setFeusTags(Joiner.on(',').join(tags.subList(idx-1, idx + 1))));

                if (idx++ < 3) {
                    feedsUsersRecords.add(FEEDS_USERS.newRecord()
                            .setFeusFeedId(fr.getFeedId())
                            .setFeusUserId(UsersRecordSamples.LSKYWALKER.getUserId()));
                }
            }
            FEEDS_USERS_RECORDS = feedsUsersRecords;
        }

        @Override
        public List<FeedsUsersRecord> records() {
            return FEEDS_USERS_RECORDS;
        }
    }
}
