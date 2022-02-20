package fr.ght1pc9kc.baywatch.tests.samples.infra;

import fr.ght1pc9kc.baywatch.dsl.tables.records.FeedsRecord;
import fr.ght1pc9kc.baywatch.dsl.tables.records.FeedsUsersRecord;
import fr.ght1pc9kc.baywatch.dsl.tables.records.UsersRecord;
import fr.irun.testy.jooq.model.RelationalDataSet;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static fr.ght1pc9kc.baywatch.dsl.tables.FeedsUsers.FEEDS_USERS;

public class FeedsUsersRecordSample implements RelationalDataSet<FeedsUsersRecord> {
    public static final FeedsUsersRecordSample SAMPLE = new FeedsUsersRecordSample();

    public static final List<FeedsUsersRecord> FEEDS_USERS_RECORDS;

    static {
        String[] tags = new String[]{
                "java,spring",
                "cpp"
        };
        // -1 allow to keep one orphan feed for tests
        FEEDS_USERS_RECORDS = IntStream.range(0, FeedRecordSamples.FEEDS_RECORDS.size() - 1)
                .mapToObj(f -> {
                    FeedsRecord feed = FeedRecordSamples.FEEDS_RECORDS.get(f);
                    List<UsersRecord> users = UsersRecordSamples.SAMPLE.records();
                    return FEEDS_USERS.newRecord()
                            .setFeusFeedId(feed.getFeedId())
                            .setFeusUserId(users.get(f % users.size()).getUserId())
                            .setFeusTags(tags[f % tags.length]);
                }).collect(Collectors.toUnmodifiableList());
    }

    @Override
    public List<FeedsUsersRecord> records() {
        return FEEDS_USERS_RECORDS;
    }
}
