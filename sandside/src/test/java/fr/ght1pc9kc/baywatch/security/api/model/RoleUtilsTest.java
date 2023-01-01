package fr.ght1pc9kc.baywatch.security.api.model;

import fr.ght1pc9kc.baywatch.common.api.model.Entity;
import fr.ght1pc9kc.baywatch.common.infra.mappers.BaywatchMapper;
import fr.ght1pc9kc.baywatch.tests.samples.infra.UsersRecordSamples;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mapstruct.factory.Mappers;

import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;

class RoleUtilsTest {
    private static final BaywatchMapper MAPPER = Mappers.getMapper(BaywatchMapper.class);

    @ParameterizedTest
    @CsvSource(delimiter = '|', textBlock = """
                                                        | MANAGER |                                 | false
                USER                                    | MANAGER |                                 | false
                MANAGER                                 | MANAGER |                                 | true
                ADMIN                                   | MANAGER |                                 | true
                SYSTEM                                  | MANAGER |                                 | true
                MANAGER:TM01GNPWYV8Z0W96J0DA8FG1TM18    | MANAGER |                                 | true
                MANAGER:TM01GNPWYV8Z0W96J0DA8FG1TM18    | MANAGER | TM01GNPWYV8Z0W96J0DA8FG1TM16    | false
                ADMIN                                   | MANAGER | TM01GNPWYV8Z0W96J0DA8FG1TM16    | true
                NOT_A_ROLE                              | USER    |                                 | false
            """)
    void should_check_user_has_role(String role, Role compare, String entity, boolean expected) {
        Entity<User> okenobi = MAPPER.recordToUserEntity(UsersRecordSamples.OKENOBI);

        assertThat(RoleUtils.hasRole(okenobi.self.withRoles(role), compare, entity)).isEqualTo(expected);
    }

    @Test
    void should_fail_upade_user_roles() {
        Entity<User> okenobi = MAPPER.recordToUserEntity(UsersRecordSamples.OKENOBI);

        assertThat(okenobi.self.withRoles("NOT_A_ROLE", Role.USER.name()).roles)
                .containsOnly(Role.USER.name());
    }

    @Test
    void should_get_system() {
        assertThat(RoleUtils.getSystemUser().self.roles).containsOnly(Role.SYSTEM.name());
    }

    @ParameterizedTest
    @CsvSource(delimiter = '|', textBlock = """
                ADMIN                                   | ROLE_ADMIN
                MANAGER:TM01GNPWYV8Z0W96J0DA8FG1TM18    | MANAGER:TM01GNPWYV8Z0W96J0DA8FG1TM18
                NONROLE                                 | \s
            """)
    void should_convert_role_to_authority(String role, String expected) {
        if (Objects.nonNull(expected))
            assertThat(RoleUtils.toSpringAuthority(role)).isEqualTo(expected);
        else
            Assertions.assertThatException()
                    .isThrownBy(() -> RoleUtils.toSpringAuthority(role));
    }

    @ParameterizedTest
    @CsvSource(delimiter = '|', textBlock = """
                ADMIN                                   | ROLE_ADMIN
                MANAGER:TM01GNPWYV8Z0W96J0DA8FG1TM18    | MANAGER:TM01GNPWYV8Z0W96J0DA8FG1TM18
                MANAGER:TM01GNPWYV8Z0W96J0DA8FG1TM18    | ROLE_MANAGER:TM01GNPWYV8Z0W96J0DA8FG1TM18
                                                        | NONROLE
            """)
    void should_convert_from_authority(String expected, String authority) {
        if (Objects.nonNull(expected))
                assertThat(RoleUtils.fromSpringAuthority(authority)).isEqualTo(expected);
        else
            Assertions.assertThatException()
                    .isThrownBy(() -> RoleUtils.fromSpringAuthority(authority));
    }
}