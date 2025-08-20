package fr.ght1pc9kc.baywatch.notify.infra.controllers;

import fr.ght1pc9kc.baywatch.security.api.model.Permission;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;

import java.util.concurrent.BlockingDeque;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingDeque;

import static fr.ght1pc9kc.baywatch.tests.samples.UserSamples.LUKE;
import static fr.ght1pc9kc.baywatch.tests.samples.UserSamples.OBIWAN;

class SseControllerTest {
    private SseController tested;

    @BeforeEach
    void setUp() {
        tested = new SseController();
    }

    @Test
    void should_get_sse_flux() {
        var authenticationToken = new PreAuthenticatedAuthenticationToken(OBIWAN, null,
                AuthorityUtils.createAuthorityList(OBIWAN.self().roles().stream()
                        .map(Permission::toString)
                        .toArray(String[]::new)));

        Flux<ServerSentEvent<String>> actualSse = tested.sse()
                .contextWrite(ReactiveSecurityContextHolder.withAuthentication(authenticationToken));

        try (ExecutorService es = Executors.newVirtualThreadPerTaskExecutor()) {
            BlockingDeque<ServerSentEvent<String>> actual = new LinkedBlockingDeque<>(5);
            Future<Disposable> disposableFuture = es.submit(() -> actualSse.subscribe(actual::add));

            Assertions.assertThat(actual.take().event()).isEqualTo("open");

            tested.send("May the Force", OBIWAN.self().login()).block();
            tested.send("Be with you", LUKE.self().login()).block();
            tested.send("Always", OBIWAN.self().login()).block();
            tested.send("Order 66", null).block();

            Assertions.assertThat(actual.take().data()).isEqualTo("[okenobi] May the Force");
            Assertions.assertThat(actual.take().data()).isEqualTo("[okenobi] Always");
            Assertions.assertThat(actual.take().data()).isEqualTo("Order 66");

            disposableFuture.get().dispose();
        } catch (InterruptedException | ExecutionException e) {
            Assertions.fail("Should not fail", e);
        }

    }
}