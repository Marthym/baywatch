package fr.ght1pc9kc.baywatch.infra.mappers;

import fr.ght1pc9kc.baywatch.api.model.User;
import fr.ght1pc9kc.baywatch.dsl.tables.records.UsersRecord;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class RecordToUserConverter implements Converter<UsersRecord, User> {
    @Override
    public User convert(UsersRecord record) {
        return User.builder()
                .id(record.getUserId())
                .login(record.getUserLogin())
                .name(record.getUserName())
                .mail(record.getUserEmail())
                .build();
    }
}
