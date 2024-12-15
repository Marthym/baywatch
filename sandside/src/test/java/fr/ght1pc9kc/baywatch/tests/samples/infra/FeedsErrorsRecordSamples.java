package fr.ght1pc9kc.baywatch.tests.samples.infra;

import fr.ght1pc9kc.baywatch.dsl.tables.FeedsErrors;
import fr.ght1pc9kc.baywatch.dsl.tables.records.FeedsErrorsRecord;
import fr.ght1pc9kc.baywatch.tests.samples.FeedSamples;
import fr.ght1pc9kc.testy.jooq.model.RelationalDataSet;

import java.time.LocalDateTime;
import java.util.List;

public class FeedsErrorsRecordSamples implements RelationalDataSet<FeedsErrorsRecord> {
    public static final FeedsErrorsRecordSamples SAMPLE = new FeedsErrorsRecordSamples();

    @Override
    public List<FeedsErrorsRecord> records() {
        return List.of(
                FeedsErrors.FEEDS_ERRORS.newRecord()
                        .setFeerFeedId(FeedSamples.JEDI.id())
                        .setFeerSince(LocalDateTime.parse("2024-03-30T12:42:24"))
                        .setFeerLastLabel("Not Found")
                        .setFeerLastStatus(404)
                        .setFeerLastTime(LocalDateTime.parse("2024-03-30T13:12:24")),
                FeedsErrors.FEEDS_ERRORS.newRecord()
                        .setFeerFeedId(FeedSamples.SITH.id())
                        .setFeerSince(LocalDateTime.parse("2024-03-30T12:42:24"))
                        .setFeerLastLabel("Not Found")
                        .setFeerLastStatus(404)
                        .setFeerLastTime(LocalDateTime.parse("2024-03-30T13:12:24"))
        );
    }
}
