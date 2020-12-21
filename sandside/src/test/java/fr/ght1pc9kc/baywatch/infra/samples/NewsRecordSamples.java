package fr.ght1pc9kc.baywatch.infra.samples;

import fr.ght1pc9kc.baywatch.api.model.Flags;
import fr.ght1pc9kc.baywatch.domain.utils.Hasher;
import fr.ght1pc9kc.baywatch.dsl.tables.News;
import fr.ght1pc9kc.baywatch.dsl.tables.NewsFeeds;
import fr.ght1pc9kc.baywatch.dsl.tables.NewsUserState;
import fr.ght1pc9kc.baywatch.dsl.tables.records.NewsFeedsRecord;
import fr.ght1pc9kc.baywatch.dsl.tables.records.NewsRecord;
import fr.ght1pc9kc.baywatch.dsl.tables.records.NewsUserStateRecord;
import fr.irun.testy.jooq.model.RelationalDataSet;

import java.time.LocalDateTime;
import java.time.Period;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class NewsRecordSamples implements RelationalDataSet<NewsRecord> {
    public static final NewsRecordSamples SAMPLE = new NewsRecordSamples();

    private static final List<NewsRecord> RECORDS;
    private static final List<NewsFeedsRecord> NEWS_FEEDS_RECORDS;
    private static final List<NewsUserStateRecord> NEWS_USER_STATE_RECORDS;

    static {
        RECORDS = IntStream.range(1, 51).mapToObj(i -> News.NEWS.newRecord()
                .setNewsId(Hasher.sha3(String.format("https://blog.ght1pc9kc.fr/%03d", i)))
                .setNewsLink(String.format("https://blog.ght1pc9kc.fr/%03d", i))
                .setNewsTitle(String.format("ght1pc9kc.fr %03d", i))
                .setNewsPublication(LocalDateTime.parse("2020-12-10T10:42:42").plus(Period.ofMonths(i)))
        ).collect(Collectors.toUnmodifiableList());

        NEWS_FEEDS_RECORDS = IntStream.range(1, 51).mapToObj(i -> {
                    int feedIdx = i % FeedRecordSamples.SAMPLE.records().size();
                    return NewsFeeds.NEWS_FEEDS.newRecord()
                            .setNefeFeedId(FeedRecordSamples.SAMPLE.records().get(feedIdx).getFeedId())
                            .setNefeNewsId(Hasher.sha3(String.format("https://blog.ght1pc9kc.fr/%03d", i)));
                }
        ).collect(Collectors.toUnmodifiableList());

        Integer[] FLAGS = new Integer[]{
                Flags.NONE, Flags.READ, Flags.STAR, Flags.ALL};
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
