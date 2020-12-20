package fr.ght1pc9kc.baywatch.infra.mappers;

import fr.ght1pc9kc.baywatch.api.model.User;
import fr.ght1pc9kc.baywatch.dsl.tables.records.UsersRecord;
import org.jooq.Field;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.util.Map;

import static fr.ght1pc9kc.baywatch.dsl.tables.Users.USERS;

@Component
public class UserToRecordConverter implements Converter<User, UsersRecord> {
    public static final Map<String, Field<?>> USER_PROPERTIES_MAPPING = Map.of(
            "id", USERS.USER_ID,
            "login", USERS.USER_LOGIN,
            "name", USERS.USER_NAME,
            "mail", USERS.USER_EMAIL
    );

    @Override
    public UsersRecord convert(User source) {
        return USERS.newRecord()
                .setUserId(source.id)
                .setUserLogin(source.login)
                .setUserName(source.name)
                .setUserEmail(source.mail);
    }
}
