package fr.ght1pc9kc.baywatch.common.infra.adapters;

import fr.ght1pc9kc.baywatch.common.api.ClientInfoFacade;
import fr.ght1pc9kc.baywatch.common.api.model.ClientInfoContext;
import fr.ght1pc9kc.baywatch.common.infra.filters.ReactiveClientInfoContextHolder;
import fr.ght1pc9kc.baywatch.common.infra.filters.ReactiveLocaleContextHolder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.i18n.LocaleContext;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import reactor.util.context.Context;

import java.net.InetSocketAddress;
import java.net.URI;
import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ClientInfoFacadeAdapterTest {
    private ClientInfoFacade tested;

    @BeforeEach
    void setUp() {
        tested = new ClientInfoFacadeAdapter();
    }

    @Test
    void should_get_locale_from_context() {
        LocaleContext mockLocaleContext = mock(LocaleContext.class);
        when(mockLocaleContext.getLocale()).thenReturn(Locale.FRENCH);

        StepVerifier.create(tested.getLocale()
                        .contextWrite(ReactiveLocaleContextHolder.withLocaleContext(Mono.just(mockLocaleContext))))
                .assertNext(locale -> assertThat(locale).isEqualTo(Locale.FRENCH))
                .verifyComplete();
    }

    @Test
    void should_get_default_locale_when_context_locale_is_null() {
        LocaleContext mockLocaleContext = mock(LocaleContext.class);
        when(mockLocaleContext.getLocale()).thenReturn(null);

        StepVerifier.create(tested.getLocale()
                        .contextWrite(ReactiveLocaleContextHolder.withLocaleContext(Mono.just(mockLocaleContext))))
                .assertNext(locale -> assertThat(locale).isEqualTo(Locale.getDefault()))
                .verifyComplete();
    }

    @Test
    void should_get_default_locale_when_no_context() {
        StepVerifier.create(tested.getLocale())
                .assertNext(locale -> assertThat(locale).isEqualTo(Locale.getDefault()))
                .verifyComplete();
    }

    @Test
    void should_get_user_agent_from_context() {
        String expectedUserAgent = "Mozilla/5.0 (Death Star OS; rv:66.6) Imperial Browser/42.0";
        ClientInfoContext clientContext = new ClientInfoContext(
                new InetSocketAddress("192.168.1.42", 8080),
                expectedUserAgent,
                URI.create("https://deathstar.empire")
        );

        StepVerifier.create(tested.getUserAgent()
                        .contextWrite(ReactiveClientInfoContextHolder.withClientInfo(
                                clientContext.ip(), clientContext.userAgent(), clientContext.baseUrl())))
                .assertNext(userAgent -> assertThat(userAgent).isEqualTo(expectedUserAgent))
                .verifyComplete();
    }

    @Test
    void should_get_remote_address_from_context() {
        // Given
        InetSocketAddress expectedAddress = new InetSocketAddress("10.0.0.1", 9090);
        ClientInfoContext clientContext = new ClientInfoContext(
                expectedAddress,
                "Jedi Browser/1.0",
                URI.create("https://jedi.temple")
        );

        // When & Then
        StepVerifier.create(tested.getRemoteAddress()
                        .contextWrite(ReactiveClientInfoContextHolder.withClientInfo(
                                clientContext.ip(), clientContext.userAgent(), clientContext.baseUrl())))
                .assertNext(address -> assertThat(address).isEqualTo(expectedAddress))
                .verifyComplete();
    }

    @Test
    void should_get_base_url_from_context() {
        // Given
        URI expectedBaseUrl = URI.create("https://rebellion.alliance");
        ClientInfoContext clientContext = new ClientInfoContext(
                new InetSocketAddress("172.16.0.42", 3000),
                "Rebel Browser/2.0",
                expectedBaseUrl
        );

        // When & Then
        StepVerifier.create(tested.getBaseUrl()
                        .contextWrite(ReactiveClientInfoContextHolder.withClientInfo(
                                clientContext.ip(), clientContext.userAgent(), clientContext.baseUrl())))
                .assertNext(baseUrl -> assertThat(baseUrl).isEqualTo(expectedBaseUrl))
                .verifyComplete();
    }

    @Test
    void should_handle_multiple_context_operations() {
        // Given
        LocaleContext mockLocaleContext = mock(LocaleContext.class);
        when(mockLocaleContext.getLocale()).thenReturn(Locale.ENGLISH);

        ClientInfoContext clientContext = new ClientInfoContext(
                new InetSocketAddress("66.6.66.6", 6666),
                "Sith Browser/3.0",
                URI.create("https://dark.side")
        );

        Context reactorContext = Context.empty()
                .putAll(ReactiveLocaleContextHolder.withLocaleContext(Mono.just(mockLocaleContext)).readOnly())
                .putAll(ReactiveClientInfoContextHolder.withClientInfo(
                        clientContext.ip(), clientContext.userAgent(), clientContext.baseUrl()).readOnly());

        StepVerifier.create(
                        Mono.zip(
                                tested.getLocale(),
                                tested.getUserAgent(),
                                tested.getRemoteAddress(),
                                tested.getBaseUrl()
                        ).contextWrite(reactorContext)
                )
                .assertNext(tuple -> {
                    assertThat(tuple.getT1()).isEqualTo(Locale.ENGLISH);
                    assertThat(tuple.getT2()).isEqualTo("Sith Browser/3.0");
                    assertThat(tuple.getT3()).isEqualTo(new InetSocketAddress("66.6.66.6", 6666));
                    assertThat(tuple.getT4()).isEqualTo(URI.create("https://dark.side"));
                })
                .verifyComplete();
    }

    @Test
    void should_handle_empty_client_info_context() {
        StepVerifier.create(tested.getUserAgent())
                .as("Devrait lever une erreur si pas de contexte")
                .verifyError();

        StepVerifier.create(tested.getRemoteAddress())
                .as("Devrait lever une erreur si pas de contexte")
                .verifyError();

        StepVerifier.create(tested.getBaseUrl())
                .as("Devrait lever une erreur si pas de contexte")
                .verifyError();
    }

    @Test
    void should_handle_various_locales() {
        Locale[] testLocales = {
                Locale.US,
                Locale.GERMANY,
                Locale.JAPAN,
                Locale.of("wookiee", "KASHYYYK")
        };

        for (Locale testLocale : testLocales) {
            LocaleContext mockLocaleContext = mock(LocaleContext.class);
            when(mockLocaleContext.getLocale()).thenReturn(testLocale);

            StepVerifier.create(tested.getLocale()
                            .contextWrite(ReactiveLocaleContextHolder.withLocaleContext(Mono.just(mockLocaleContext))))
                    .assertNext(locale -> assertThat(locale).isEqualTo(testLocale))
                    .verifyComplete();
        }
    }

    @Test
    void should_handle_various_client_info_scenarios() {
        ClientInfoContext[] testContexts = {
                new ClientInfoContext(
                        new InetSocketAddress("127.0.0.1", 80),
                        "Chrome/100.0",
                        URI.create("http://localhost")
                ),
                new ClientInfoContext(
                        new InetSocketAddress("192.168.1.100", 8443),
                        "Firefox/99.0",
                        URI.create("https://secure.site")
                ),
                new ClientInfoContext(
                        new InetSocketAddress("10.0.0.5", 443),
                        "Mobile Safari/14.0",
                        URI.create("https://mobile.app")
                )
        };

        for (ClientInfoContext context : testContexts) {
            Context reactorContext = ReactiveClientInfoContextHolder.withClientInfo(
                    context.ip(), context.userAgent(), context.baseUrl());

            StepVerifier.create(
                            Mono.zip(
                                    tested.getUserAgent(),
                                    tested.getRemoteAddress(),
                                    tested.getBaseUrl()
                            ).contextWrite(reactorContext)
                    )
                    .assertNext(tuple -> {
                        assertThat(tuple.getT1()).isEqualTo(context.userAgent());
                        assertThat(tuple.getT2()).isEqualTo(context.ip());
                        assertThat(tuple.getT3()).isEqualTo(context.baseUrl());
                    })
                    .verifyComplete();
        }
    }

}