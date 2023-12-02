package fr.ght1pc9kc.baywatch.security.infra.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import fr.ght1pc9kc.baywatch.security.api.model.Permission;
import fr.ght1pc9kc.baywatch.security.api.model.Role;
import fr.ght1pc9kc.testy.core.extensions.WithObjectMapper;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.Optional;

class PermissionMixinTest {

    @RegisterExtension
    public WithObjectMapper withObjectMapper = WithObjectMapper.builder()
            .addMixin(Permission.class, PermissionMixin.class)
            .build();

    @ParameterizedTest
    @CsvSource(delimiter = '|', textBlock = """
            ADMIN       | ADMIN     |
            MANAGER:42  | MANAGER   | 42
            USER        | USER      |
            """)
    void should_convert_string_to_permissions(String value, Role expectedRole, String expectedEntity, ObjectMapper tested) {
        Permission actual = tested.convertValue(value, Permission.class);
        Assertions.assertThat(actual.role()).isEqualTo(expectedRole);
        Assertions.assertThat(actual.entity()).isEqualTo(Optional.ofNullable(expectedEntity));
        if (expectedEntity == null) {
            Assertions.assertThat(actual).isEqualTo(expectedRole);
        }
    }
}