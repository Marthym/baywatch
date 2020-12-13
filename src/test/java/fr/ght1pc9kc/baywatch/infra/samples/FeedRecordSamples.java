package fr.ght1pc9kc.baywatch.infra.samples;

import fr.ght1pc9kc.baywatch.dsl.tables.records.FeedsRecord;
import fr.irun.testy.jooq.model.RelationalDataSet;

import java.time.LocalDateTime;
import java.util.List;

import static fr.ght1pc9kc.baywatch.dsl.tables.Feeds.FEEDS;

public class FeedRecordSamples implements RelationalDataSet<FeedsRecord> {
    public static final FeedRecordSamples SAMPLES = new FeedRecordSamples();

    public static final FeedsRecord JEDI = FEEDS.newRecord()
            .setFeedId(42)
            .setFeedName("Jedi")
            .setFeedUrl("http://obiwan.kenobi.jedi/")
            .setFeedLastWatch(LocalDateTime.parse("2020-12-11T15:12:42"));

    @Override
    public List<FeedsRecord> records() {
        return List.of(
                JEDI,
                JEDI.copy().setFeedId(43),
                JEDI.copy().setFeedId(44),
                JEDI.copy().setFeedId(45),
                JEDI.copy().setFeedId(46)
        );
    }
}
