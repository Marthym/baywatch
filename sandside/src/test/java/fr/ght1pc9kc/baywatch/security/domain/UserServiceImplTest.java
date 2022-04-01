package fr.ght1pc9kc.baywatch.security.domain;

import fr.ght1pc9kc.baywatch.common.api.model.EntitiesProperties;
import fr.ght1pc9kc.baywatch.common.api.model.Entity;
import fr.ght1pc9kc.baywatch.security.api.UserService;
import fr.ght1pc9kc.baywatch.security.api.model.User;
import fr.ght1pc9kc.baywatch.security.domain.exceptions.UnauthenticatedUser;
import fr.ght1pc9kc.baywatch.security.domain.exceptions.UnauthorizedOperation;
import fr.ght1pc9kc.baywatch.security.api.AuthenticationFacade;
import fr.ght1pc9kc.baywatch.security.domain.ports.UserPersistencePort;
import fr.ght1pc9kc.baywatch.tests.samples.UserSamples;
import fr.ght1pc9kc.baywatch.techwatch.domain.model.QueryContext;
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
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UserServiceImplTest {
    private static final Instant CURRENT = Instant.parse("2022-02-08T22:57:00Z");
    private final UserPersistencePort mockUserRepository = mock(UserPersistencePort.class);
    private final AuthenticationFacade mockAuthFacade = mock(AuthenticationFacade.class);

    private UserService tested;

    @BeforeEach
    void setUp() {
        when(mockAuthFacade.getConnectedUser()).thenReturn(Mono.just(UserSamples.YODA));
        when(mockUserRepository.get(any())).thenReturn(Mono.just(UserSamples.LUKE));
        when(mockUserRepository.list(any())).thenReturn(Flux.just(UserSamples.LUKE, UserSamples.OBIWAN, UserSamples.YODA));
        when(mockUserRepository.persist(any())).thenReturn(Flux.just(UserSamples.LUKE, UserSamples.OBIWAN, UserSamples.YODA));
        when(mockUserRepository.count(any())).thenReturn(Mono.just(3));
        //noinspection deprecation
        tested = new UserServiceImpl(mockUserRepository, mockAuthFacade, NoOpPasswordEncoder.getInstance(),
                Clock.fixed(CURRENT, ZoneOffset.UTC));
    }

    @Test
    void should_get_user() {
        StepVerifier.create(tested.get(UserSamples.LUKE.id))
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
    void should_create_user_without_authentication() {
        when(mockAuthFacade.getConnectedUser()).thenReturn(Mono.empty());

        StepVerifier.create(tested.create(UserSamples.OBIWAN.self))
                .verifyError(UnauthenticatedUser.class);
    }

    @Test
    void should_create_user_without_admin() {
        when(mockAuthFacade.getConnectedUser()).thenReturn(Mono.just(UserSamples.LUKE));

        StepVerifier.create(tested.create(UserSamples.OBIWAN.self))
                .verifyError(UnauthorizedOperation.class);
    }

    @Test
    @SuppressWarnings("unchecked")
    void should_create_user_with_admin() {
        when(mockAuthFacade.getConnectedUser()).thenReturn(Mono.just(UserSamples.YODA));
        when(mockUserRepository.persist(any())).thenReturn(Flux.just(UserSamples.OBIWAN));

        StepVerifier.create(tested.create(UserSamples.OBIWAN.self))
                .expectNext(UserSamples.OBIWAN)
                .verifyComplete();

        ArgumentCaptor<List<Entity<User>>> users = ArgumentCaptor.forClass(List.class);
        verify(mockUserRepository).persist(users.capture());

        Entity<User> actual = users.getValue().get(0);
        Assertions.assertThat(actual).isEqualTo(Entity.identify(UserSamples.OBIWAN.id, CURRENT, UserSamples.OBIWAN.self));
    }

    @Test
    @SuppressWarnings("unchecked")
    void should_delete_users_as_admin() {
        when(mockAuthFacade.getConnectedUser()).thenReturn(Mono.just(UserSamples.YODA));
        when(mockUserRepository.list(any())).thenReturn(Flux.just(UserSamples.OBIWAN));
        when(mockUserRepository.delete(anyCollection())).thenReturn(Mono.just(1));

        StepVerifier.create(tested.delete(List.of(UserSamples.OBIWAN.id)))
                .expectNext(UserSamples.OBIWAN)
                .verifyComplete();

        ArgumentCaptor<QueryContext> selected = ArgumentCaptor.forClass(QueryContext.class);
        verify(mockUserRepository).list(selected.capture());
        ArgumentCaptor<List<String>> deleted = ArgumentCaptor.forClass(List.class);
        verify(mockUserRepository).delete(deleted.capture());

        Assertions.assertThat(selected.getValue())
                .isEqualTo(QueryContext.all(Criteria.property(EntitiesProperties.ID).in(UserSamples.OBIWAN.id)));
        Assertions.assertThat(deleted.getValue()).isEqualTo(List.of(UserSamples.OBIWAN.id));
    }
}