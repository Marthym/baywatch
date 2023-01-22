package fr.ght1pc9kc.baywatch.security.api.model;

import fr.ght1pc9kc.baywatch.tests.samples.UserSamples;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;

class RoleUtilsTest {
    @ParameterizedTest
    @CsvSource(delimiter = '|', textBlock = """
                                                        | MANAGER |                                 | false
                USER                                    | MANAGER |                                 | false
                USER                                    | USER    |                                 | true
                MANAGER                                 | MANAGER |                                 | true
                ADMIN                                   | MANAGER |                                 | true
                SYSTEM                                  | MANAGER |                                 | true
                MANAGER:TM01GNPWYV8Z0W96J0DA8FG1TM18    | MANAGER |                                 | true
                MANAGER:TM01GNPWYV8Z0W96J0DA8FG1TM18    | MANAGER | TM01GNPWYV8Z0W96J0DA8FG1TM16    | false
                ADMIN                                   | MANAGER | TM01GNPWYV8Z0W96J0DA8FG1TM16    | true
                MANAGER                                 | MANAGER | TM01GNPWYV8Z0W96J0DA8FG1TM16    | false
                NOT_A_ROLE                              | USER    |                                 | false
            """)
    void should_check_user_has_role(String role, Role compare, String entity, boolean expected) {
        assertThat(RoleUtils.hasPermission(UserSamples.OBIWAN.self.withRoles(role), Permission.of(compare, entity)))
                .isEqualTo(expected);
    }

    @Test
    void should_fail_upade_user_roles() {
        assertThat(UserSamples.OBIWAN.self.withRoles("NOT_A_ROLE", Role.USER.name()).roles)
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