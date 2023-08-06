package fr.ght1pc9kc.baywatch.security.infra.adapters;

import fr.ght1pc9kc.baywatch.common.api.model.Entity;
import fr.ght1pc9kc.baywatch.common.domain.DateUtils;
import fr.ght1pc9kc.baywatch.dsl.tables.records.UsersRecord;
import fr.ght1pc9kc.baywatch.dsl.tables.records.UsersRolesRecord;
import fr.ght1pc9kc.baywatch.security.api.model.Permission;
import fr.ght1pc9kc.baywatch.security.api.model.UpdatableUser;
import fr.ght1pc9kc.baywatch.security.api.model.User;
import org.jooq.Record;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValueCheckStrategy;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static fr.ght1pc9kc.baywatch.dsl.tables.Users.USERS;
import static fr.ght1pc9kc.baywatch.dsl.tables.UsersRoles.USERS_ROLES;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserMapper {

    UpdatableUser getUpdatableUser(Map<String, Object> userForm);

    @SuppressWarnings({"OptionalAssignedToNull", "java:S2789", "java:S3655"})
    default UsersRecord updatableUserToRecord(UpdatableUser user) {
        var r = new UsersRecord();
        if (user.login() != null) {
            r.setUserLogin(user.login());
        }
        if (user.name() != null) {
            if (user.name().isPresent())
                r.setUserName(user.name().get());
            else {
                r.setUserName(null);
            }
        }
        if (user.mail() != null) {
            r.setUserEmail(user.mail());
        }
        if (user.password() != null) {
            r.setUserPassword(user.password());
        }
        return r;
    }

    @Mapping(expression = "java( role.toString() )", target = "usroRole")
    UsersRolesRecord permissionToRecord(Permission role);

    @Mapping(source = "id", target = "userId")
    @Mapping(source = "createdAt", target = "userCreatedAt")
    @Mapping(source = "self.login", target = "userLogin")
    @Mapping(source = "self.name", target = "userName")
    @Mapping(source = "self.mail", target = "userEmail")
    @Mapping(source = "self.password", target = "userPassword",
            nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS,
            nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    UsersRecord entityUserToRecord(Entity<User> user);

    default Entity<User> recordToUserEntity(Record usersRecord) {
        List<Permission> permissions = Arrays.stream(usersRecord.get(USERS_ROLES.USRO_ROLE).split(","))
                .map(Permission::from)
                .distinct()
                .sorted(Permission.COMPARATOR)
                .toList();

        return Entity.identify(usersRecord.get(USERS.USER_ID), DateUtils.toInstant(usersRecord.get(USERS.USER_CREATED_AT)),
                User.builder()
                        .login(usersRecord.get(USERS.USER_LOGIN))
                        .mail(usersRecord.get(USERS.USER_EMAIL))
                        .name(usersRecord.get(USERS.USER_NAME))
                        .password(usersRecord.get(USERS.USER_PASSWORD))
                        .roles(permissions)
                        .build());
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
            throw new IllegalArgumentException("Permission list can not be null or empty !");
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
