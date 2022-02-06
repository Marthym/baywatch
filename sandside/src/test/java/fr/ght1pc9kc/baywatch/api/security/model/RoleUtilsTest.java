package fr.ght1pc9kc.baywatch.api.security.model;

import fr.ght1pc9kc.baywatch.api.common.model.Entity;
import fr.ght1pc9kc.baywatch.infra.common.mappers.BaywatchMapper;
import fr.ght1pc9kc.baywatch.infra.samples.UsersRecordSamples;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import static org.assertj.core.api.Assertions.assertThat;

class RoleUtilsTest {
    private static final BaywatchMapper MAPPER = Mappers.getMapper(BaywatchMapper.class);

    @Test
    void should_check_user_has_role() {
        Entity<User> okenobi = MAPPER.recordToUserEntity(UsersRecordSamples.OKENOBI);

        assertThat(RoleUtils.hasRole(okenobi.entity.withRole(Role.ANONYMOUS), Role.MANAGER)).isFalse();
        assertThat(RoleUtils.hasRole(okenobi.entity.withRole(Role.USER), Role.MANAGER)).isFalse();
        assertThat(RoleUtils.hasRole(okenobi.entity.withRole(Role.MANAGER), Role.MANAGER)).isTrue();
        assertThat(RoleUtils.hasRole(okenobi.entity.withRole(Role.ADMIN), Role.MANAGER)).isTrue();
        assertThat(RoleUtils.hasRole(okenobi.entity.withRole(Role.SYSTEM), Role.MANAGER)).isTrue();
    }
}