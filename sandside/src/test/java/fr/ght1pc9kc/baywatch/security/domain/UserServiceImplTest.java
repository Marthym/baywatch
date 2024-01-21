package fr.ght1pc9kc.baywatch.security.domain;

import com.github.f4b6a3.ulid.Ulid;
import com.github.f4b6a3.ulid.UlidFactory;
import fr.ght1pc9kc.baywatch.common.api.model.EntitiesProperties;
import fr.ght1pc9kc.baywatch.notify.api.model.BasicEvent;
import fr.ght1pc9kc.baywatch.notify.api.model.EventType;
import fr.ght1pc9kc.baywatch.security.api.AuthenticationFacade;
import fr.ght1pc9kc.baywatch.security.api.model.Permission;
import fr.ght1pc9kc.baywatch.security.api.model.Role;
import fr.ght1pc9kc.baywatch.security.api.model.UpdatableUser;
import fr.ght1pc9kc.baywatch.security.api.model.User;
import fr.ght1pc9kc.baywatch.security.domain.exceptions.UnauthorizedOperation;
import fr.ght1pc9kc.baywatch.security.domain.ports.AuthorizationPersistencePort;
import fr.ght1pc9kc.baywatch.security.domain.ports.NotificationPort;
import fr.ght1pc9kc.baywatch.security.domain.ports.UserPersistencePort;
import fr.ght1pc9kc.baywatch.techwatch.domain.model.QueryContext;
import fr.ght1pc9kc.baywatch.tests.samples.UserSamples;
import fr.ght1pc9kc.entity.api.Entity;
import fr.ght1pc9kc.juery.api.Criteria;
import fr.ght1pc9kc.juery.api.PageRequest;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UserServiceImplTest {
    private static final Instant CURRENT = Instant.parse("2022-02-08T22:57:00Z");
    private final UserPersistencePort mockUserRepository = mock(UserPersistencePort.class);
    private final AuthorizationPersistencePort mockAuthorizationRepository = mock(AuthorizationPersistencePort.class);
    private final AuthenticationFacade mockAuthFacade = mock(AuthenticationFacade.class);
    private final UlidFactory mockUlidFactory = spy(UlidFactory.newMonotonicInstance());
    private final NotificationPort mockNotificationPort = mock(NotificationPort.class);

    private UserServiceImpl tested;

    @BeforeEach
    @SuppressWarnings("deprecation")
    void setUp() {
        when(mockAuthFacade.getConnectedUser()).thenReturn(Mono.just(UserSamples.YODA));
        doAnswer(answer -> Stream.of(UserSamples.LUKE, UserSamples.YODA, UserSamples.OBIWAN)
                .filter(u -> u.id().equals(answer.getArgument(0, String.class)))
                .findAny().map(Mono::just).orElseThrow()
        ).when(mockUserRepository).get(anyString());
        when(mockUserRepository.list(any())).thenReturn(Flux.just(UserSamples.LUKE, UserSamples.OBIWAN, UserSamples.YODA));
        doAnswer(answer -> Flux.fromIterable(answer.getArgument(0, Collection.class)))
                .when(mockUserRepository).persist(anyCollection());
        doAnswer(answer -> Mono.just(UserSamples.LUKE)).when(mockUserRepository).persist(anyString(), anyCollection());
        when(mockUserRepository.delete(anyString(), anyCollection())).thenReturn(Mono.empty().then());
        when(mockUserRepository.count(any())).thenReturn(Mono.just(3));
        when(mockAuthorizationRepository.count(any())).thenReturn(Mono.just(0));
        when(mockNotificationPort.send(anyString(), any(EventType.class), any()))
                .thenReturn(new BasicEvent<>(Ulid.fast().toString(), EventType.USER_NOTIFICATION, "New jedi in the force"));

        tested = new UserServiceImpl(mockUserRepository, mockAuthorizationRepository, mockNotificationPort,
                mockAuthFacade, NoOpPasswordEncoder.getInstance(),
                Clock.fixed(CURRENT, ZoneOffset.UTC), mockUlidFactory);
    }

    @Test
    void should_get_user() {
        StepVerifier.create(tested.get(UserSamples.LUKE.id()))
                .expectNext(UserSamples.LUKE)
                .verifyComplete();
    }

    @Test
    void should_list_users() {
        StepVerifier.create(tested.list(PageRequest.all()))
                .expectNext(UserSamples.LUKE)
                .expectNext(UserSamples.OBIWAN)
                .expectNext(UserSamples.YODA)
                .verifyComplete();
    }

    @Test
    void should_count_users() {
        StepVerifier.create(tested.count(PageRequest.all()))
                .expectNext(3)
                .verifyComplete();
    }

    @Test
    @SuppressWarnings("unchecked")
    void should_create_user_without_authentication() {
        when(mockAuthFacade.getConnectedUser()).thenReturn(Mono.empty());

        StepVerifier.create(tested.create(UserSamples.OBIWAN.self()))
                .expectNextCount(1)
                .verifyComplete();

        ArgumentCaptor<List<Entity<User>>> actuals = ArgumentCaptor.forClass(List.class);
        verify(mockUserRepository).persist(actuals.capture());

        Entity<User> actual = actuals.getValue().getFirst();
        assertAll(
                () -> Assertions.assertThat(actual.id()).isNotBlank(),
                () -> Assertions.assertThat(actual.createdBy()).isEqualTo(actual.id()),
                () -> Assertions.assertThat(actual.createdAt()).isEqualTo(CURRENT),
                () -> Assertions.assertThat(actual.self()).isEqualTo(UserSamples.OBIWAN.self())
        );
    }

    @Test
    void should_create_user_without_admin() {
        when(mockAuthFacade.getConnectedUser()).thenReturn(Mono.just(UserSamples.LUKE));

        StepVerifier.create(tested.create(UserSamples.OBIWAN.self()))
                .verifyError(UnauthorizedOperation.class);
    }

    @Test
    @SuppressWarnings("unchecked")
    void should_create_user_with_admin() {
        when(mockAuthFacade.getConnectedUser()).thenReturn(Mono.just(UserSamples.YODA));
        when(mockUlidFactory.create()).thenReturn(Ulid.from(UserSamples.OBIWAN.id().substring(2)));

        StepVerifier.create(tested.create(UserSamples.OBIWAN.self()))
                .expectNextCount(1)
                .verifyComplete();

        ArgumentCaptor<List<Entity<User>>> users = ArgumentCaptor.forClass(List.class);
        verify(mockUserRepository).persist(users.capture());

        Entity<User> actual = users.getValue().getFirst();
        assertAll(
                () -> Assertions.assertThat(actual.id()).isNotBlank(),
                () -> Assertions.assertThat(actual.createdBy()).isEqualTo(UserSamples.YODA.id()),
                () -> Assertions.assertThat(actual.createdAt()).isEqualTo(CURRENT),
                () -> Assertions.assertThat(actual.self()).isEqualTo(UserSamples.OBIWAN.self())
        );
    }

    @Test
    void should_update_other_user_as_admin() {
        when(mockAuthFacade.getConnectedUser()).thenReturn(Mono.just(UserSamples.YODA));
        when(mockUserRepository.update(anyString(), any())).thenReturn(Mono.just(UserSamples.OBIWAN));
        UpdatableUser obiChan = UserSamples.OBIWAN.self().updatable()
                .name(Optional.of("Obi Chan"))
                .build();

        Mono<Entity<User>> actual = tested.update(UserSamples.OBIWAN.id(), obiChan, "kenobi");

        StepVerifier.create(actual)
                .assertNext(a -> Assertions.assertThat(a).isNotNull())
                .verifyComplete();

        verify(mockUserRepository).update(UserSamples.OBIWAN.id(), obiChan);
    }

    @Test
    void should_update_my_user() {
        when(mockAuthFacade.getConnectedUser()).thenReturn(Mono.just(UserSamples.OBIWAN));
        when(mockUserRepository.update(anyString(), any())).thenReturn(Mono.just(UserSamples.OBIWAN));
        UpdatableUser obiChan = UserSamples.OBIWAN.self().updatable()
                .name(Optional.of("Obi Chan"))
                .build();

        Mono<Entity<User>> actual = tested.update(UserSamples.OBIWAN.id(), obiChan, UserSamples.OBIWAN.self().password);

        StepVerifier.create(actual)
                .assertNext(a -> Assertions.assertThat(a).isNotNull())
                .verifyComplete();

        verify(mockUserRepository).update(UserSamples.OBIWAN.id(), obiChan);
    }

    @Test
    void should_fail_update_user_with_invalid_password() {
        when(mockAuthFacade.getConnectedUser()).thenReturn(Mono.just(UserSamples.OBIWAN));
        when(mockUserRepository.update(anyString(), any())).thenReturn(Mono.just(UserSamples.OBIWAN));
        UpdatableUser obiChan = UserSamples.OBIWAN.self().updatable()
                .name(Optional.of("Obi Chan"))
                .build();

        Mono<Entity<User>> actual = tested.update(UserSamples.OBIWAN.id(), obiChan, "Invalid Password");

        StepVerifier.create(actual)
                .verifyError(UnauthorizedOperation.class);

        verify(mockUserRepository, never()).update(UserSamples.OBIWAN.id(), obiChan);
    }

    @Test
    @SuppressWarnings("unchecked")
    void should_delete_users_as_admin() {
        when(mockAuthFacade.getConnectedUser()).thenReturn(Mono.just(UserSamples.YODA));
        when(mockUserRepository.list(any())).thenReturn(Flux.just(UserSamples.OBIWAN));
        when(mockUserRepository.delete(anyCollection())).thenReturn(Mono.just(1));

        StepVerifier.create(tested.delete(List.of(UserSamples.OBIWAN.id())))
                .expectNext(UserSamples.OBIWAN)
                .verifyComplete();

        ArgumentCaptor<QueryContext> selected = ArgumentCaptor.forClass(QueryContext.class);
        verify(mockUserRepository).list(selected.capture());
        ArgumentCaptor<List<String>> deleted = ArgumentCaptor.forClass(List.class);
        verify(mockUserRepository).delete(deleted.capture());

        Assertions.assertThat(selected.getValue())
                .isEqualTo(QueryContext.all(Criteria.property(EntitiesProperties.ID).in(UserSamples.OBIWAN.id())));
        Assertions.assertThat(deleted.getValue()).isEqualTo(List.of(UserSamples.OBIWAN.id()));
    }

    @Test
    void should_grant_role_as_admin() {
        when(mockAuthFacade.getConnectedUser()).thenReturn(Mono.just(UserSamples.YODA));

        StepVerifier.create(tested.grants(UserSamples.LUKE.id(), List.of(Permission.manager("42"))))
                .expectNext(UserSamples.LUKE)
                .verifyComplete();

        verify(mockUserRepository).persist(UserSamples.LUKE.id(), List.of("MANAGER:42"));
    }

    @Test
    void should_grant_role_as_user() {
        when(mockAuthFacade.getConnectedUser()).thenReturn(Mono.just(UserSamples.LUKE));

        StepVerifier.create(tested.grants(UserSamples.LUKE.id(), List.of(Permission.manager("42"))))
                .expectNext(UserSamples.LUKE)
                .verifyComplete();

        verify(mockUserRepository).persist(UserSamples.LUKE.id(), List.of("MANAGER:42"));
    }

    @Test
    void should_fail_to_elevate_self_role() {
        when(mockAuthFacade.getConnectedUser()).thenReturn(Mono.just(UserSamples.LUKE));

        StepVerifier.create(tested.grants(UserSamples.LUKE.id(), List.of(Role.ADMIN)))
                .verifyError(UnauthorizedOperation.class);
    }

    @Test
    void should_fail_to_grant_other_user() {
        when(mockAuthFacade.getConnectedUser()).thenReturn(Mono.just(UserSamples.LUKE));

        StepVerifier.create(tested.grants(UserSamples.OBIWAN.id(), List.of(Permission.manager("42"))))
                .verifyError(UnauthorizedOperation.class);
    }

    @Test
    void should_revoke_role_as_admin() {
        when(mockAuthFacade.getConnectedUser()).thenReturn(Mono.just(UserSamples.YODA));

        StepVerifier.create(tested.revokes(Permission.manager("42"), List.of(UserSamples.LUKE.id())))
                .expectNext()
                .verifyComplete();

        verify(mockUserRepository).delete("MANAGER:42", List.of(UserSamples.LUKE.id()));
    }

    @Test
    void should_revoke_role_as_user() {
        when(mockAuthFacade.getConnectedUser()).thenReturn(Mono.just(UserSamples.LUKE));

        StepVerifier.create(tested.revokes(Permission.manager("42"), List.of(UserSamples.LUKE.id())))
                .expectNext()
                .verifyComplete();

        verify(mockUserRepository).delete("MANAGER:42", List.of(UserSamples.LUKE.id()));
    }

    @Test
    void should_fail_to_revoke_other_user() {
        when(mockAuthFacade.getConnectedUser()).thenReturn(Mono.just(UserSamples.LUKE));

        StepVerifier.create(tested.revokes(Role.MANAGER, List.of(UserSamples.OBIWAN.id())))
                .verifyError(UnauthorizedOperation.class);
    }
}