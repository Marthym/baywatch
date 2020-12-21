package fr.ght1pc9kc.baywatch.infra.mappers;

import fr.ght1pc9kc.baywatch.api.model.Feed;
import fr.ght1pc9kc.baywatch.dsl.tables.FeedsUsers;
import fr.ght1pc9kc.baywatch.dsl.tables.records.FeedsUsersRecord;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class FeedsUsersToRecordConverter implements Converter<Feed, FeedsUsersRecord> {
    @Override
    public FeedsUsersRecord convert(Feed source) {
        String tags = String.join(",", source.getTags());
        return FeedsUsers.FEEDS_USERS.newRecord()
                .setFeusFeedId(source.getId())
                .setFeusTags(tags);
    }
}
