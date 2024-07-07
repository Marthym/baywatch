package fr.ght1pc9kc.baywatch.common.infra.filters;

import fr.ght1pc9kc.baywatch.common.api.model.UserMeta;
import fr.ght1pc9kc.baywatch.tests.samples.UserSamples;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.net.InetSocketAddress;

class ReactiveClientInfoContextHolderTest {

    @Test
    void should_read_and_write_context() {

        StepVerifier.create(Mono.just(UserSamples.OBIWAN)
                        .flatMap(u -> ReactiveClientInfoContextHolder.getContext().map(ctx -> u.withMeta(UserMeta.loginIP, ctx.ip().toString())))
                        .contextWrite(ReactiveClientInfoContextHolder.withClientInfo(
                                InetSocketAddress.createUnresolved("127.0.0.1", 80), "Dummy User Agent")))
                .assertNext(user -> Assertions.assertThat(user.meta(UserMeta.loginIP)).contains("127.0.0.1/<unresolved>:80"))
                .verifyComplete();
    }

    @Test
    void should_clear_context() {
        StepVerifier.create(Mono.just(UserSamples.OBIWAN)
                        .flatMap(u -> ReactiveClientInfoContextHolder.getContext()
                                .map(ctx -> u.withMeta(UserMeta.loginIP, ctx.ip().toString()))
                                .switchIfEmpty(Mono.just(u)))
                        .contextWrite(ReactiveClientInfoContextHolder.clearContext())
                        .contextWrite(ReactiveClientInfoContextHolder.withClientInfo(
                                InetSocketAddress.createUnresolved("127.0.0.1", 80), "Dummy User Agent")))
                .assertNext(user -> Assertions.assertThat(user.meta(UserMeta.loginIP)).isEmpty())
                .verifyComplete();
    }
}