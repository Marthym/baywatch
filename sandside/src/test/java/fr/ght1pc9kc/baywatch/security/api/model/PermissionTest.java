package fr.ght1pc9kc.baywatch.security.api.model;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Stream;

class PermissionTest {
    @Test
    void should_convert_list() {
        List<Permission> actual = Stream.of("MANAGER:42", "ADMIN", "USER")
                .map(Permission::from)
                .distinct()
                .sorted(Permission.COMPARATOR)
                .toList();

        Assertions.assertThat(actual).containsExactly(Role.ADMIN, new Authorization(Role.MANAGER, "42"), Role.USER);
    }
}