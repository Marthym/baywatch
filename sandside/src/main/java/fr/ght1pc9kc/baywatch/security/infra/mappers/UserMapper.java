package fr.ght1pc9kc.baywatch.security.infra.mappers;

import fr.ght1pc9kc.baywatch.common.domain.DateUtils;
import fr.ght1pc9kc.baywatch.dsl.tables.records.UsersRecord;
import fr.ght1pc9kc.baywatch.dsl.tables.records.UsersRolesRecord;
import fr.ght1pc9kc.baywatch.security.api.model.Permission;
import fr.ght1pc9kc.baywatch.security.api.model.User;
import fr.ght1pc9kc.baywatch.security.infra.model.UserForm;
import fr.ght1pc9kc.entity.api.Entity;
import org.jooq.Record;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static fr.ght1pc9kc.baywatch.common.api.model.EntitiesProperties.LOGIN;
import static fr.ght1pc9kc.baywatch.common.api.model.EntitiesProperties.MAIL;
import static fr.ght1pc9kc.baywatch.common.api.model.EntitiesProperties.NAME;
import static fr.ght1pc9kc.baywatch.common.api.model.EntitiesProperties.PASSWORD;
import static fr.ght1pc9kc.baywatch.common.api.model.EntitiesProperties.ROLES;
import static fr.ght1pc9kc.baywatch.common.api.model.UserMeta.createdAt;
import static fr.ght1pc9kc.baywatch.common.api.model.UserMeta.createdBy;
import static fr.ght1pc9kc.baywatch.common.api.model.UserMeta.loginAt;
import static fr.ght1pc9kc.baywatch.common.api.model.UserMeta.loginIP;
import static fr.ght1pc9kc.baywatch.dsl.tables.Users.USERS;
import static fr.ght1pc9kc.baywatch.dsl.tables.UsersRoles.USERS_ROLES;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserMapper {

    default User getUpdatableUser(Map<String, Object> userForm) {
        Objects.requireNonNull(userForm.get(LOGIN), "Login is required for User");
        Collection<? extends Permission> roles = mapPermissions(userForm.getOrDefault(ROLES, List.of()));

        return User.builder()
                .login(userForm.get(LOGIN).toString())
                .name(userForm.containsKey(NAME) ? userForm.get(NAME).toString() : null)
                .mail(userForm.containsKey(MAIL) ? userForm.get(MAIL).toString() : null)
                .password(userForm.containsKey(PASSWORD) ? userForm.get(PASSWORD).toString() : null)
                .roles(roles)
                .build();
    }

    User getUser(UserForm userForm);

    @Mapping(expression = "java( role.toString() )", target = "usroRole")
    UsersRolesRecord permissionToRecord(Permission role);

    default UsersRecord entityUserToRecord(Entity<User> user) {
        UsersRecord usersRecord = USERS.newRecord()
                .setUserId(user.id())
                .setUserLogin(user.self().login());
        user.meta(createdAt, Instant.class)
                .map(DateUtils::toLocalDateTime)
                .ifPresent(usersRecord::setUserCreatedAt);
        user.meta(createdBy).ifPresent(usersRecord::setUserCreatedBy);
        user.meta(loginAt, Instant.class)
                .map(DateUtils::toLocalDateTime)
                .ifPresent(usersRecord::setUserLoginAt);
        user.meta(loginIP).ifPresent(usersRecord::setUserLoginIp);

        if (Objects.nonNull(user.self().name())) {
            usersRecord.setUserName(user.self().name());
        }
        if (Objects.nonNull(user.self().mail())) {
            usersRecord.setUserEmail(user.self().mail());
        }

        if (Objects.nonNull(user.self().password())) {
            usersRecord.setUserPassword(user.self().password());
        }
        return usersRecord;
    }

    default Entity<User> recordToUserEntity(Record usersRecord) {
        String roles = usersRecord.get(USERS_ROLES.USRO_ROLE);
        List<Permission> permissions = (roles == null) ?
                List.of() :
                Arrays.stream(roles.split(","))
                        .map(Permission::from)
                        .distinct()
                        .sorted(Permission.COMPARATOR)
                        .toList();


        return Entity.identify(User.builder()
                        .login(usersRecord.get(USERS.USER_LOGIN))
                        .mail(usersRecord.get(USERS.USER_EMAIL))
                        .name(usersRecord.get(USERS.USER_NAME))
                        .password(usersRecord.get(USERS.USER_PASSWORD))
                        .roles(permissions)
                        .build())
                .meta(createdAt, DateUtils.toInstant(usersRecord.get(USERS.USER_CREATED_AT)))
                .meta(createdBy, usersRecord.get(USERS.USER_CREATED_BY))
                .meta(loginAt, DateUtils.toInstant(usersRecord.get(USERS.USER_LOGIN_AT)))
                .meta(loginIP, usersRecord.get(USERS.USER_LOGIN_IP))
                .withId(usersRecord.get(USERS.USER_ID));
    }

    default LocalDateTime map(Instant value) {
        return DateUtils.toLocalDateTime(value);
    }

    default String map(Object value) {
        return (value == null) ? null : value.toString();
    }

    default Optional<String> mapOptional(Object value) {
        return Optional.ofNullable(value).map(Object::toString);
    }

    default List<Permission> mapPermissions(Object value) {
        if (value == null) {
            throw new IllegalArgumentException("Permission list can not be null !");
        }
        if (value instanceof List<?> values) {
            return values.stream()
                    .map(Object::toString)
                    .map(Permission::from)
                    .toList();
        } else {
            throw new IllegalArgumentException("value must be a list of Permissions");
        }
    }
}
