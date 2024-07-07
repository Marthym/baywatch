package fr.ght1pc9kc.baywatch.security.infra.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import fr.ght1pc9kc.baywatch.common.infra.model.Page;
import fr.ght1pc9kc.baywatch.security.api.AuthorizationService;
import fr.ght1pc9kc.baywatch.security.api.UserService;
import fr.ght1pc9kc.baywatch.security.api.model.Permission;
import fr.ght1pc9kc.baywatch.security.api.model.User;
import fr.ght1pc9kc.baywatch.security.infra.adapters.UserMapper;
import fr.ght1pc9kc.baywatch.security.infra.config.PermissionMixin;
import fr.ght1pc9kc.baywatch.security.infra.config.UserMixin;
import fr.ght1pc9kc.baywatch.security.infra.model.UserForm;
import fr.ght1pc9kc.baywatch.security.infra.model.UserSearchRequest;
import fr.ght1pc9kc.baywatch.tests.samples.UserSamples;
import fr.ght1pc9kc.juery.api.PageRequest;
import fr.ght1pc9kc.testy.core.extensions.WithObjectMapper;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.mapstruct.factory.Mappers;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

class UserGqlControllerTest {

    @RegisterExtension
    private static final WithObjectMapper wMapper = WithObjectMapper.builder()
            .addMixin(User.class, UserMixin.class)
            .addMixin(Permission.class, PermissionMixin.class)
            .build();

    private UserGqlController tested;

    @BeforeEach
    void setUp(ObjectMapper mapper) {
        UserService mockUserService = mock(UserService.class);
        doReturn(Flux.fromIterable(UserSamples.SAMPLES)).when(mockUserService).list(any(PageRequest.class));
        doReturn(Mono.just(UserSamples.SAMPLES.size())).when(mockUserService).count(any(PageRequest.class));
        doReturn(Mono.just(UserSamples.YODA)).when(mockUserService).create(any(User.class));
        doReturn(Mono.just(UserSamples.YODA)).when(mockUserService).update(any(), anyString());
        doReturn(Flux.fromIterable(UserSamples.SAMPLES)).when(mockUserService).delete(anyCollection());
        AuthorizationService mockAuthService = mock(AuthorizationService.class);
        doReturn(Mono.just(UserSamples.YODA)).when(mockAuthService).grants(anyString(), anyCollection());
        UserMapper userMapper = Mappers.getMapper(UserMapper.class);
        tested = new UserGqlController(mockUserService, mockAuthService, mapper, userMapper);
    }

    @Test
    void should_call_userSearch() {
        StepVerifier.create(tested.userSearch(new UserSearchRequest(
                        0, 3, null, null, null, "42", null, null, null)))
                .assertNext(actual -> SoftAssertions.assertSoftly(soft -> {
                    soft.assertThat(actual).isNotNull();
                    soft.assertThat(actual.getBody()).isNotNull();
                    StepVerifier.create(Objects.requireNonNull(actual.getBody()))
                            .expectNextCount(2)
                            .verifyComplete();
                })).verifyComplete();
    }

    @Test
    void should_create_user() {
        StepVerifier.create(tested.userCreate(new UserForm(
                "okenobi", "Obiwan Kenobi",
                "okenobi@jedi.com", "MayThe4th", List.of("MANAGER"))
        )).assertNext(actual -> SoftAssertions.assertSoftly(soft -> {
            soft.assertThat(actual).isNotNull();
            soft.assertThat(actual.get("_id").toString()).isEqualTo("US01GRQ11X1W8E6NQER7E1FNQ7HC");
        })).verifyComplete();
    }

    @Test
    void should_update_user() {
        StepVerifier.create(tested.userUpdate("US01GRQ11X1W8E6NQER7E1FNQ7HC", "obiwan", Map.of(
                "login", "okenobi",
                "name", "Kenobi Obichan"
        ))).assertNext(actual -> SoftAssertions.assertSoftly(soft -> {
            soft.assertThat(actual).isNotNull();
            soft.assertThat(actual.get("_id").toString()).isEqualTo("US01GRQ11X1W8E6NQER7E1FNQ7HC");
        })).verifyComplete();
    }

    @Test
    void should_grant_user() {
        StepVerifier.create(tested.userGrants("US01GRQ11X1W8E6NQER7E1FNQ7HC", List.of("MANAGER")))
                .assertNext(actual -> SoftAssertions.assertSoftly(soft -> {
                    soft.assertThat(actual).isNotNull();
                    soft.assertThat(actual.get("_id").toString()).isEqualTo("US01GRQ11X1W8E6NQER7E1FNQ7HC");
                })).verifyComplete();
    }

    @Test
    void should_delete_user() {
        StepVerifier.create(tested.userDelete(List.of("US01GRQ11X1W8E6NQER7E1FNQ7HC")))
                .assertNext(actual -> SoftAssertions.assertSoftly(soft -> {
                    soft.assertThat(actual).isNotNull();
                    soft.assertThat(actual.get("_id").toString()).isEqualTo("US01GRQ11XKGHERDEBSCHBNJAY78");
                }))
                .expectNextCount(1)
                .verifyComplete();
    }

    @Test
    void should_fail_to_delete() {
        StepVerifier.create(tested.userDelete(List.of())).verifyComplete();
        StepVerifier.create(tested.userDelete(null)).verifyComplete();
    }

    @Test
    void should_map_entities() {
        StepVerifier.create(tested.entities(Page.of(Flux.fromIterable(UserSamples.SAMPLES), UserSamples.SAMPLES.size())))
                .assertNext(actual -> SoftAssertions.assertSoftly(soft -> {
                    soft.assertThat(actual).containsKeys("_id", "login");
                }))
                .expectNextCount(1)
                .verifyComplete();
    }

    @Test
    void should_map_totalCount() {
        StepVerifier.create(tested.totalCount(Page.of(Flux.fromIterable(UserSamples.SAMPLES), UserSamples.SAMPLES.size())))
                .assertNext(actual -> SoftAssertions.assertSoftly(soft -> {
                    soft.assertThat(actual).isEqualTo(2);
                })).verifyComplete();
    }
}