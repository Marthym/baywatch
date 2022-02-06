package fr.ght1pc9kc.baywatch.domain.security;

import fr.ght1pc9kc.baywatch.api.common.model.Entity;
import fr.ght1pc9kc.baywatch.api.security.UserService;
import fr.ght1pc9kc.baywatch.api.security.model.Role;
import fr.ght1pc9kc.baywatch.api.security.model.User;
import fr.ght1pc9kc.baywatch.domain.exceptions.UnauthorizedOperation;
import fr.ght1pc9kc.baywatch.domain.ports.AuthenticationFacade;
import fr.ght1pc9kc.baywatch.domain.ports.UserPersistencePort;
import fr.ght1pc9kc.baywatch.domain.security.samples.UserSamples;
import fr.ght1pc9kc.baywatch.domain.utils.Hasher;
import fr.ght1pc9kc.juery.api.PageRequest;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class UserServiceImplTest {
    private final UserPersistencePort mockUserRepository = mock(UserPersistencePort.class);
    private final AuthenticationFacade mockAuthFacade = mock(AuthenticationFacade.class);

    private static final Entity<User> TO_BE_CREATED = Entity.identify(
            Hasher.sha3("obiwan.kenobi@jedi.com"),
            User.builder()
                    .login("okenobi")
                    .name("Obiwan Kenobi")
                    .mail("obiwan.kenobi@jedi.com")
                    .password("mayThe4th")
                    .role(Role.USER)
                    .build());

    private UserService tested;

    @BeforeEach
    void setUp() {
        when(mockUserRepository.get(any())).thenReturn(Mono.just(UserSamples.LUKE));
        when(mockUserRepository.list(any())).thenReturn(Flux.just(UserSamples.LUKE, UserSamples.OBIWAN, UserSamples.YODA));
        when(mockUserRepository.persist(any())).thenReturn(Flux.just(UserSamples.LUKE, UserSamples.OBIWAN, UserSamples.YODA));
        when(mockUserRepository.count(any())).thenReturn(Mono.just(3));
        tested = new UserServiceImpl(mockUserRepository, mockAuthFacade);
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
        when(mockAuthFacade.getConnectedUser()).thenReturn(Mono.empty());
        StepVerifier.create(tested.count(PageRequest.all()))
                .expectNext(3)
                .verifyComplete();
    }

    @Test
    void should_create_user_without_admin() {
        when(mockAuthFacade.getConnectedUser()).thenReturn(Mono.empty());

        StepVerifier.create(tested.create(TO_BE_CREATED.entity))
                .verifyError(UnauthorizedOperation.class);
    }

    @Test
    @SuppressWarnings("unchecked")
    void should_create_user_with_admin() {
        when(mockAuthFacade.getConnectedUser()).thenReturn(Mono.just(UserSamples.YODA));
        when(mockUserRepository.persist(any())).thenReturn(Flux.just(UserSamples.OBIWAN));
        UserPersistencePort spyed = spy(mockUserRepository);

        StepVerifier.create(tested.create(TO_BE_CREATED.entity))
                .expectNext(UserSamples.OBIWAN)
                .verifyComplete();

        ArgumentCaptor<List<Entity<User>>> users = ArgumentCaptor.forClass(List.class);
        verify(spyed).persist(users.capture());

        Entity<User> actual = users.getValue().get(0);
        Assertions.assertThat(actual).isEqualTo(TO_BE_CREATED);
    }
}