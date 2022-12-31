package fr.ght1pc9kc.baywatch.security.api.model;

import fr.ght1pc9kc.baywatch.common.api.model.Entity;
import fr.ght1pc9kc.baywatch.common.infra.mappers.BaywatchMapper;
import fr.ght1pc9kc.baywatch.tests.samples.infra.UsersRecordSamples;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import static org.assertj.core.api.Assertions.assertThat;

class RoleUtilsTest {
    private static final BaywatchMapper MAPPER = Mappers.getMapper(BaywatchMapper.class);

    @Test
    void should_check_user_has_role() {
        Entity<User> okenobi = MAPPER.recordToUserEntity(UsersRecordSamples.OKENOBI);

        assertThat(RoleUtils.hasRole(okenobi.self.withRoles(Role.ANONYMOUS), Role.MANAGER)).isFalse();
        assertThat(RoleUtils.hasRole(okenobi.self.withRoles(Role.USER), Role.MANAGER)).isFalse();
        assertThat(RoleUtils.hasRole(okenobi.self.withRoles(Role.MANAGER), Role.MANAGER)).isTrue();
        assertThat(RoleUtils.hasRole(okenobi.self.withRoles(Role.ADMIN), Role.MANAGER)).isTrue();
        assertThat(RoleUtils.hasRole(okenobi.self.withRoles(Role.SYSTEM), Role.MANAGER)).isTrue();
    }
}