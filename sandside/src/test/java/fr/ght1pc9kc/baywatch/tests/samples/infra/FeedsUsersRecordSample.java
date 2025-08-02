package fr.ght1pc9kc.baywatch.tests.samples.infra;

import fr.ght1pc9kc.baywatch.dsl.tables.records.FeedsRecord;
import fr.ght1pc9kc.baywatch.dsl.tables.records.FeedsUsersRecord;
import fr.ght1pc9kc.baywatch.dsl.tables.records.UsersRecord;
import fr.ght1pc9kc.testy.jooq.model.RelationalDataSet;

import java.util.ArrayList;
import java.util.List;

import static fr.ght1pc9kc.baywatch.dsl.tables.FeedsUsers.FEEDS_USERS;

public class FeedsUsersRecordSample implements RelationalDataSet<FeedsUsersRecord> {
    public static final FeedsUsersRecordSample SAMPLE = new FeedsUsersRecordSample();

    public static final List<FeedsUsersRecord> FEEDS_USERS_RECORDS;

    static {
        ArrayList<FeedsUsersRecord> records = new ArrayList<>(FeedRecordSamples.FEEDS_RECORDS.size());
        int f = 0;
        for (FeedsRecord feedsRecord : FeedRecordSamples.FEEDS_RECORDS) {
            if (!feedsRecord.getFeedVisible()) {
                continue;
            }
            List<UsersRecord> users = UsersRecordSamples.SAMPLE.records();
            records.add(FEEDS_USERS.newRecord()
                    .setFeusFeedId(feedsRecord.getFeedId())
                    .setFeusUserId(users.get(f % users.size()).getUserId()));
            ++f;
        }
        // allow keeping one orphan feed for tests
        records.removeLast();

        records.addAll(List.of(
                // Link the first FEED to Obiwan AND Luke
                FEEDS_USERS.newRecord()
                        .setFeusFeedId(FeedRecordSamples.FEEDS_RECORDS.get(1).getFeedId())
                        .setFeusUserId(UsersRecordSamples.SAMPLE.records().getFirst().getUserId()),
                // Link the personal feeds
                FEEDS_USERS.newRecord()
                        .setFeusFeedId(FeedRecordSamples.OBIWAN_PERSONAL.getFeedId())
                        .setFeusUserId(FeedRecordSamples.OBIWAN_PERSONAL.getFeedId()),
                FEEDS_USERS.newRecord()
                        .setFeusFeedId(FeedRecordSamples.LUKE_PERSONAL.getFeedId())
                        .setFeusUserId(FeedRecordSamples.LUKE_PERSONAL.getFeedId())
        ));

        FEEDS_USERS_RECORDS = List.copyOf(records);
    }

    @Override
    public List<FeedsUsersRecord> records() {
        return FEEDS_USERS_RECORDS;
    }
}
