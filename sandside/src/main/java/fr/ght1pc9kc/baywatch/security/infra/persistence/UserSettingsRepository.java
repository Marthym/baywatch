package fr.ght1pc9kc.baywatch.security.infra.persistence;

import fr.ght1pc9kc.baywatch.common.infra.DatabaseQualifier;
import fr.ght1pc9kc.baywatch.dsl.tables.records.UsersSettingsRecord;
import fr.ght1pc9kc.baywatch.security.api.model.UserSettings;
import fr.ght1pc9kc.baywatch.security.domain.ports.UserSettingsPersistencePort;
import fr.ght1pc9kc.baywatch.security.infra.mappers.UserSettingsMapper;
import fr.ght1pc9kc.entity.api.Entity;
import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;

import static fr.ght1pc9kc.baywatch.dsl.tables.UsersSettings.USERS_SETTINGS;

@Repository
@RequiredArgsConstructor
@SuppressWarnings("BlockingMethodInNonBlockingContext")
public class UserSettingsRepository implements UserSettingsPersistencePort {
    private final DSLContext dsl;
    private final @DatabaseQualifier Scheduler dbScheduler;
    private final UserSettingsMapper mapper;

    public Mono<Entity<UserSettings>> get(String id) {
        return Mono.fromCallable(() ->
                        dsl.select()
                                .from(USERS_SETTINGS)
                                .where(USERS_SETTINGS.USSE_USER_ID.eq(id))
                                .fetchOne()).subscribeOn(dbScheduler)
                .map(mapper::getUserSettingsEntity);
    }

    public Mono<Entity<UserSettings>> persist(String id, UserSettings userSettings) {
        UsersSettingsRecord userSettingsRecord = mapper.getUserSettingsRecord(userSettings);
        UsersSettingsRecord updateRecord = userSettingsRecord.copy();
        userSettingsRecord.setUsseUserId(id);
        return Mono.fromCallable(() ->
                        dsl.insertInto(USERS_SETTINGS)
                                .set(userSettingsRecord)
                                .onDuplicateKeyUpdate()
                                .set(updateRecord)
                                .execute())
                .subscribeOn(dbScheduler)
                .then(get(id));
    }
}
