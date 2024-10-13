package fr.ght1pc9kc.baywatch.tests.samples.infra;

import fr.ght1pc9kc.baywatch.dsl.tables.FeedsUsersProperties;
import fr.ght1pc9kc.baywatch.dsl.tables.records.FeedsUsersPropertiesRecord;
import fr.ght1pc9kc.baywatch.techwatch.infra.model.FeedProperties;
import fr.ght1pc9kc.testy.jooq.model.RelationalDataSet;

import java.util.List;

public class FeedsUsersPropertiesRecordSample implements RelationalDataSet<FeedsUsersPropertiesRecord> {
    public static final FeedsUsersPropertiesRecordSample SAMPLE = new FeedsUsersPropertiesRecordSample();

    private static final FeedsUsersPropertiesRecord OBIWAN_JEDI_NAME_PROPERTIES = FeedsUsersProperties.FEEDS_USERS_PROPERTIES.newRecord()
            .setFuprFeedId(FeedRecordSamples.JEDI.getFeedId())
            .setFuprUserId(UsersRecordSamples.OKENOBI.getUserId())
            .setFuprPropertyName(FeedProperties.NAME.name())
            .setFuprPropertyValue("Customized for Obiwan JEDI Name");

    private static final FeedsUsersPropertiesRecord OBIWAN_JEDI_TAG1_PROPERTIES = FeedsUsersProperties.FEEDS_USERS_PROPERTIES.newRecord()
            .setFuprFeedId(FeedRecordSamples.JEDI.getFeedId())
            .setFuprUserId(UsersRecordSamples.OKENOBI.getUserId())
            .setFuprPropertyName(FeedProperties.TAG.name())
            .setFuprPropertyValue("light");

    private static final FeedsUsersPropertiesRecord OBIWAN_JEDI_TAG2_PROPERTIES = FeedsUsersProperties.FEEDS_USERS_PROPERTIES.newRecord()
            .setFuprFeedId(FeedRecordSamples.JEDI.getFeedId())
            .setFuprUserId(UsersRecordSamples.OKENOBI.getUserId())
            .setFuprPropertyName(FeedProperties.TAG.name())
            .setFuprPropertyValue("republic");

    @Override
    public List<FeedsUsersPropertiesRecord> records() {
        return List.of(
                OBIWAN_JEDI_NAME_PROPERTIES,
                OBIWAN_JEDI_TAG1_PROPERTIES,
                OBIWAN_JEDI_TAG2_PROPERTIES
        );
    }
}
