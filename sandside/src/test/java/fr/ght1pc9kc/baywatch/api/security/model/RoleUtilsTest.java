package fr.ght1pc9kc.baywatch.api.security.model;

import fr.ght1pc9kc.baywatch.infra.mappers.BaywatchMapper;
import fr.ght1pc9kc.baywatch.infra.samples.UsersRecordSamples;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import static org.assertj.core.api.Assertions.assertThat;

class RoleUtilsTest {
    private static final BaywatchMapper MAPPER = Mappers.getMapper(BaywatchMapper.class);

    @Test
    void should_check_user_has_role() {
        User okenobi = MAPPER.recordToUser(UsersRecordSamples.OKENOBI);

        assertThat(RoleUtils.hasRole(okenobi.withRole(Role.ANONYMOUS), Role.MANAGER)).isFalse();
        assertThat(RoleUtils.hasRole(okenobi.withRole(Role.USER), Role.MANAGER)).isFalse();
        assertThat(RoleUtils.hasRole(okenobi.withRole(Role.MANAGER), Role.MANAGER)).isTrue();
        assertThat(RoleUtils.hasRole(okenobi.withRole(Role.ADMIN), Role.MANAGER)).isTrue();
        assertThat(RoleUtils.hasRole(okenobi.withRole(Role.SYSTEM), Role.MANAGER)).isTrue();
    }
}