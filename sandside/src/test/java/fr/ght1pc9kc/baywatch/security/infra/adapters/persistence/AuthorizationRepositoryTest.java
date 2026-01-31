package fr.ght1pc9kc.baywatch.security.infra.adapters.persistence;

import fr.ght1pc9kc.baywatch.security.api.model.Permission;
import fr.ght1pc9kc.baywatch.security.api.model.Role;
import fr.ght1pc9kc.baywatch.tests.samples.UserSamples;
import fr.ght1pc9kc.baywatch.tests.samples.infra.UsersRecordSamples;
import fr.ght1pc9kc.baywatch.tests.samples.infra.UsersRolesSamples;
import fr.ght1pc9kc.testy.core.extensions.ChainedExtension;
import fr.ght1pc9kc.testy.jooq.WithDslContext;
import fr.ght1pc9kc.testy.jooq.WithInMemoryDatasource;
import fr.ght1pc9kc.testy.jooq.WithSampleDataLoaded;
import org.assertj.core.api.Assertions;
import org.jooq.DSLContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import reactor.core.scheduler.Schedulers;
import reactor.test.StepVerifier;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static fr.ght1pc9kc.baywatch.dsl.tables.UsersRoles.USERS_ROLES;
import static org.assertj.core.api.Assertions.assertThat;

class AuthorizationRepositoryTest {
    private static final WithInMemoryDatasource wDs = WithInMemoryDatasource.builder().build();
    private static final WithDslContext wDslContext = WithDslContext.builder()
            .setDatasourceExtension(wDs).build();
    private static final WithSampleDataLoaded wSamples = WithSampleDataLoaded.builder(wDslContext)
            .createTablesIfNotExists()
            .addDataset(UsersRecordSamples.SAMPLE)
            .addDataset(UsersRolesSamples.SAMPLE)
            .build();

    @RegisterExtension
    @SuppressWarnings("unused")
    static ChainedExtension chain = ChainedExtension.outer(wDs)
            .append(wDslContext)
            .append(wSamples)
            .register();

    private AuthorizationRepository tested;

    @BeforeEach
    void setUp(DSLContext dslContext) {
        tested = new AuthorizationRepository(Schedulers.immediate(), dslContext);
    }

    @Test
    void should_count_permissions() {
        StepVerifier.create(tested.count(List.of(Role.MANAGER.toString(), Role.USER.toString())))
                .expectNext(2)
                .verifyComplete();
    }

    @Test
    void should_return_zero_when_permissions_empty() {
        StepVerifier.create(tested.count(List.of()))
                .expectNext(0)
                .verifyComplete();
    }

    @Test
    void should_list_permissions_for_users(WithSampleDataLoaded.Tracker tracker) {
        tracker.skipNextSampleLoad();

        StepVerifier.create(tested.list(List.of(UserSamples.OBIWAN.id(), UserSamples.LUKE.id()))
                        .collectMap(Map.Entry::getKey, Map.Entry::getValue))
                .assertNext(actual -> Assertions.assertThat(actual)
                        .containsEntry(UserSamples.OBIWAN.id(), Set.of(Role.MANAGER))
                        .containsEntry(UserSamples.LUKE.id(), Set.of(Role.USER, Permission.manager("TM01GP696RFPTY32WD79CVB0KDTF"))))
                .verifyComplete();
    }

    @Test
    void should_fail_when_user_ids_null() {
        StepVerifier.create(tested.list((Collection<String>) null))
                .verifyError(IllegalArgumentException.class);
    }

    @Test
    void should_return_empty_when_user_ids_empty() {
        StepVerifier.create(tested.list(List.of()))
                .verifyComplete();
    }

    @Test
    void should_list_grantees_for_role() {
        StepVerifier.create(tested.grantees(Role.MANAGER))
                .expectNext(UserSamples.OBIWAN.id())
                .verifyComplete();
    }

    @Test
    void should_remove_permissions(DSLContext dsl) {
        assertThat(dsl.fetchCount(USERS_ROLES)).isEqualTo(3);

        StepVerifier.create(tested.remove(List.of(Role.MANAGER, Permission.manager("TM01GP696RFPTY32WD79CVB0KDTF"))))
                .verifyComplete();

        assertThat(dsl.fetchCount(USERS_ROLES)).isEqualTo(1);
        List<String> roles = dsl.selectFrom(USERS_ROLES).fetch(USERS_ROLES.USRO_ROLE);
        assertThat(roles).containsExactly(Role.USER.toString());
    }
}
