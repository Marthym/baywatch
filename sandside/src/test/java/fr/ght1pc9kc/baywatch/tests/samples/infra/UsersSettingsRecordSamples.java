package fr.ght1pc9kc.baywatch.tests.samples.infra;

import fr.ght1pc9kc.baywatch.dsl.tables.UsersSettings;
import fr.ght1pc9kc.baywatch.dsl.tables.records.UsersSettingsRecord;
import fr.ght1pc9kc.baywatch.security.api.model.NewsViewType;
import fr.ght1pc9kc.testy.jooq.model.RelationalDataSet;

import java.util.List;
import java.util.Locale;

public class UsersSettingsRecordSamples implements RelationalDataSet<UsersSettingsRecord> {
    public static final UsersSettingsRecordSamples SAMPLE = new UsersSettingsRecordSamples();

    public static final UsersSettingsRecord OKENOBI_SETTINGS = UsersSettings.USERS_SETTINGS.newRecord()
            .setUsseUserId(UsersRecordSamples.OKENOBI.getUserId())
            .setUssePreferredLocale(Locale.FRANCE.toLanguageTag())
            .setUsseAutoread(true)
            .setUsseNewsView(NewsViewType.MAGAZINE.name());

    public static final UsersSettingsRecord LSKYWALKER_SETTINGS = UsersSettings.USERS_SETTINGS.newRecord()
            .setUsseUserId(UsersRecordSamples.LSKYWALKER.getUserId())
            .setUssePreferredLocale(Locale.JAPAN.toLanguageTag())
            .setUsseAutoread(true)
            .setUsseNewsView(NewsViewType.MAGAZINE.name());

    public static final UsersSettingsRecord DSIDIOUS_SETTINGS = UsersSettings.USERS_SETTINGS.newRecord()
            .setUsseUserId(UsersRecordSamples.DSIDIOUS.getUserId())
            .setUssePreferredLocale(Locale.ENGLISH.toLanguageTag())
            .setUsseAutoread(false)
            .setUsseNewsView(NewsViewType.CARD.name());

    @Override
    public List<UsersSettingsRecord> records() {
        return List.of(OKENOBI_SETTINGS, LSKYWALKER_SETTINGS);
    }
}
