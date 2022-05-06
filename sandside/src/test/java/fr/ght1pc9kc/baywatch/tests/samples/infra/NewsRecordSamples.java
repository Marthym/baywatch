package fr.ght1pc9kc.baywatch.tests.samples.infra;

import fr.ght1pc9kc.baywatch.techwatch.api.model.Flags;
import fr.ght1pc9kc.baywatch.common.domain.Hasher;
import fr.ght1pc9kc.baywatch.dsl.tables.News;
import fr.ght1pc9kc.baywatch.dsl.tables.NewsFeeds;
import fr.ght1pc9kc.baywatch.dsl.tables.NewsUserState;
import fr.ght1pc9kc.baywatch.dsl.tables.records.FeedsRecord;
import fr.ght1pc9kc.baywatch.dsl.tables.records.NewsFeedsRecord;
import fr.ght1pc9kc.baywatch.dsl.tables.records.NewsRecord;
import fr.ght1pc9kc.baywatch.dsl.tables.records.NewsUserStateRecord;
import fr.irun.testy.jooq.model.RelationalDataSet;

import java.net.URI;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class NewsRecordSamples implements RelationalDataSet<NewsRecord> {
    public static final NewsRecordSamples SAMPLE = new NewsRecordSamples();
    public static final URI BASE_TEST_URI = URI.create("https://blog.ght1pc9kc.fr/");
    public static final String NEWS_MULTI_FEED_ID = Hasher.identify(BASE_TEST_URI.resolve(String.format("%03d", 51)));

    private static final List<NewsRecord> RECORDS;
    private static final List<NewsFeedsRecord> NEWS_FEEDS_RECORDS;
    private static final List<NewsUserStateRecord> NEWS_USER_STATE_RECORDS;

    static {
        RECORDS = IntStream.range(1, 52).mapToObj(i -> News.NEWS.newRecord()
                .setNewsId(Hasher.identify(BASE_TEST_URI.resolve(String.format("%03d", i))))
                .setNewsLink(BASE_TEST_URI.resolve(String.format("%03d", i)).toString())
                .setNewsTitle(String.format("%s %03d", BASE_TEST_URI.getHost(), i))
                .setNewsPublication(LocalDateTime.parse("2020-12-10T10:42:42").plus(Period.ofMonths(i)))
        ).collect(Collectors.toUnmodifiableList());

        NEWS_FEEDS_RECORDS = buildNewsFeedRecords();

        Integer[] FLAGS = new Integer[]{
                Flags.NONE, Flags.READ, Flags.SHARED, Flags.ALL};
        String[] USERS = new String[]{
                UsersRecordSamples.OKENOBI.getUserId(),
                UsersRecordSamples.LSKYWALKER.getUserId()};
        NEWS_USER_STATE_RECORDS = IntStream.range(1, 51).mapToObj(i ->
                NewsUserState.NEWS_USER_STATE.newRecord()
                        .setNursNewsId(RECORDS.get(i % RECORDS.size()).getNewsId())
                        .setNursUserId(USERS[i % USERS.length])
                        .setNursState(FLAGS[i % FLAGS.length])
        ).collect(Collectors.toUnmodifiableList());
    }

    public static List<NewsFeedsRecord> buildNewsFeedRecords() {
        List<NewsFeedsRecord> temps = new ArrayList<>(53);
        for (int i = 1; i < 51; i++) {
            int feedIdx = i % FeedRecordSamples.SAMPLE.records().size();
            NewsFeedsRecord record = NewsFeeds.NEWS_FEEDS.newRecord()
                    .setNefeFeedId(FeedRecordSamples.SAMPLE.records().get(feedIdx).getFeedId())
                    .setNefeNewsId(Hasher.identify(BASE_TEST_URI.resolve(String.format("%03d", i))));
            temps.add(record);
        }

        // Add record for multi-feed news
        for (FeedsRecord rFeed : FeedRecordSamples.SAMPLE.records()) {
            NewsFeedsRecord record = NewsFeeds.NEWS_FEEDS.newRecord()
                    .setNefeFeedId(rFeed.getFeedId())
                    .setNefeNewsId(NEWS_MULTI_FEED_ID);
            temps.add(record);
        }

        return List.copyOf(temps);
    }

    @Override
    public List<NewsRecord> records() {
        return RECORDS;
    }

    public static final class NewsFeedsRecordSample implements RelationalDataSet<NewsFeedsRecord> {
        public static final NewsFeedsRecordSample SAMPLE = new NewsFeedsRecordSample();

        @Override
        public List<NewsFeedsRecord> records() {
            return NEWS_FEEDS_RECORDS;
        }
    }

    public static final class NewsUserStateSample implements RelationalDataSet<NewsUserStateRecord> {
        public static final NewsUserStateSample SAMPLE = new NewsUserStateSample();

        @Override
        public List<NewsUserStateRecord> records() {
            return NEWS_USER_STATE_RECORDS;
        }
    }
}
