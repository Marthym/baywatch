package fr.ght1pc9kc.baywatch.infra.samples;

import fr.ght1pc9kc.baywatch.domain.utils.Hasher;
import fr.ght1pc9kc.baywatch.dsl.tables.records.FeedsRecord;
import fr.irun.testy.jooq.model.RelationalDataSet;

import java.time.LocalDateTime;
import java.util.List;

import static fr.ght1pc9kc.baywatch.dsl.tables.Feeds.FEEDS;

public class FeedRecordSamples implements RelationalDataSet<FeedsRecord> {
    public static final FeedRecordSamples SAMPLE = new FeedRecordSamples();

    public static final FeedsRecord JEDI = FEEDS.newRecord()
            .setFeedId(Hasher.sha3("http://obiwan.kenobi.jedi/"))
            .setFeedName("Jedi")
            .setFeedUrl("http://obiwan.kenobi.jedi/")
            .setFeedLastWatch(LocalDateTime.parse("2020-12-11T15:12:42"));

    public static final List<FeedsRecord> FEEDS_RECORDS = List.of(
            JEDI,
            JEDI.copy().setFeedId(Hasher.sha3(JEDI.getFeedUrl() + "01")).setFeedUrl(JEDI.getFeedUrl() + "01"),
            JEDI.copy().setFeedId(Hasher.sha3(JEDI.getFeedUrl() + "02")).setFeedUrl(JEDI.getFeedUrl() + "02"),
            JEDI.copy().setFeedId(Hasher.sha3(JEDI.getFeedUrl() + "03")).setFeedUrl(JEDI.getFeedUrl() + "03"),
            JEDI.copy().setFeedId(Hasher.sha3(JEDI.getFeedUrl() + "04")).setFeedUrl(JEDI.getFeedUrl() + "04")
    );

    @Override
    public List<FeedsRecord> records() {
        return FEEDS_RECORDS;
    }
}
