package fr.ght1pc9kc.baywatch.security.infra.mappers;

import fr.ght1pc9kc.baywatch.dsl.tables.records.UsersSettingsRecord;
import fr.ght1pc9kc.baywatch.security.api.model.NewsViewType;
import fr.ght1pc9kc.baywatch.security.api.model.UserSettings;
import fr.ght1pc9kc.baywatch.security.infra.model.UserSettingsForm;
import fr.ght1pc9kc.entity.api.Entity;
import org.jooq.Record;
import org.mapstruct.Mapper;

import java.util.Locale;

import static fr.ght1pc9kc.baywatch.dsl.tables.UsersSettings.USERS_SETTINGS;

@Mapper(componentModel = "spring")
public interface UserSettingsMapper {
    default Entity<UserSettings> getUserSettingsEntity(Record r) {
        return Entity.identify(new UserSettings(
                Locale.forLanguageTag(r.get(USERS_SETTINGS.USSE_PREFERRED_LOCALE)),
                r.get(USERS_SETTINGS.USSE_AUTOREAD),
                NewsViewType.valueOf(r.get(USERS_SETTINGS.USSE_NEWS_VIEW)))
        ).withId(r.get(USERS_SETTINGS.USSE_USER_ID));
    }

    default UsersSettingsRecord getUserSettingsRecord(UserSettings settings) {
        return USERS_SETTINGS.newRecord()
                .setUssePreferredLocale(settings.preferredLocale().toLanguageTag())
                .setUsseAutoread(settings.autoread())
                .setUsseNewsView(settings.newsViewMode().name());
    }

    UserSettings get(UserSettingsForm form);
}
